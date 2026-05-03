"""
Chat session and message endpoints.
"""
from datetime import datetime
from typing import Optional

from fastapi import APIRouter, Depends, Header, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from backend.auth import get_current_user_id
from backend.database import get_db
from backend.models import AdminReport, ChatMessage, ChatSession

router = APIRouter(prefix="/chat", tags=["Chat Sessions"])


class ChatSessionCreateRequest(BaseModel):
    session_id: str


class ChatSessionUpdateRequest(BaseModel):
    end_time: Optional[str] = None
    mood_report: Optional[str] = None
    is_active: Optional[bool] = None


class ChatSessionResponse(BaseModel):
    id: str
    user_id: int
    start_time: str
    end_time: Optional[str] = None
    mood_report: Optional[str] = None
    is_active: bool


class ChatMessageCreateRequest(BaseModel):
    id: str
    session_id: str
    content: str
    is_from_user: bool
    timestamp: Optional[str] = None


class ChatMessageResponse(BaseModel):
    id: str
    session_id: str
    user_id: int
    content: str
    is_from_user: bool
    timestamp: str


def _session_to_resp(session: ChatSession) -> ChatSessionResponse:
    return ChatSessionResponse(
        id=session.id,
        user_id=session.user_id,
        start_time=session.start_time.isoformat(),
        end_time=session.end_time.isoformat() if session.end_time else None,
        mood_report=session.mood_report,
        is_active=session.is_active,
    )


def _get_owned_session(db: Session, session_id: str, user_id: int) -> ChatSession:
    session = db.query(ChatSession).filter(
        ChatSession.id == session_id,
        ChatSession.user_id == user_id,
    ).first()
    if not session:
        raise HTTPException(status_code=404, detail="Session not found")
    return session


def _build_admin_report_content(session: ChatSession, messages: list[ChatMessage]) -> str:
    if session.mood_report and session.mood_report.strip():
        return session.mood_report.strip()

    if not messages:
        return "用户将该聊天会话提交给管理员，但会话中还没有消息内容。"

    transcript = []
    for msg in messages[-20:]:
        speaker = "用户" if msg.is_from_user else "AI"
        transcript.append(f"{speaker}: {msg.content}")
    return "用户提交了一段聊天会话，请管理员复核。\n\n" + "\n".join(transcript)


@router.post("/sessions", response_model=ChatSessionResponse)
def create_session(
    req: ChatSessionCreateRequest,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    existing = db.query(ChatSession).filter(ChatSession.id == req.session_id).first()
    if existing:
        if existing.user_id != user_id:
            raise HTTPException(status_code=403, detail="Cannot access this session")
        return _session_to_resp(existing)
    session = ChatSession(id=req.session_id, user_id=user_id)
    db.add(session)
    db.commit()
    db.refresh(session)
    return _session_to_resp(session)


@router.get("/sessions", response_model=list[ChatSessionResponse])
def get_sessions(
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    sessions = db.query(ChatSession).filter(ChatSession.user_id == user_id)\
        .order_by(ChatSession.start_time.desc()).all()
    return [_session_to_resp(session) for session in sessions]


@router.get("/sessions/active", response_model=Optional[ChatSessionResponse])
def get_active_session(
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    session = db.query(ChatSession).filter(
        ChatSession.user_id == user_id,
        ChatSession.is_active == True,
    ).first()
    return _session_to_resp(session) if session else None


@router.put("/sessions/{session_id}", response_model=dict)
def update_session(
    session_id: str,
    req: ChatSessionUpdateRequest,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    session = _get_owned_session(db, session_id, user_id)
    if req.end_time is not None:
        try:
            session.end_time = datetime.fromisoformat(req.end_time)
        except ValueError:
            pass
    if req.mood_report is not None:
        session.mood_report = req.mood_report
    if req.is_active is not None:
        session.is_active = req.is_active
    db.commit()
    return {"message": "Updated successfully"}


@router.delete("/sessions/{session_id}", response_model=dict)
def delete_session(
    session_id: str,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    session = _get_owned_session(db, session_id, user_id)
    db.query(ChatMessage).filter(ChatMessage.session_id == session_id).delete()
    db.delete(session)
    db.commit()
    return {"message": "Deleted successfully"}


@router.post("/sessions/deactivate-all", response_model=dict)
def deactivate_all_sessions(
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    db.query(ChatSession).filter(
        ChatSession.user_id == user_id,
        ChatSession.is_active == True,
    ).update({"is_active": False})
    db.commit()
    return {"message": "All sessions deactivated"}


@router.put("/sessions/{session_id}/send-to-admin", response_model=dict)
def send_session_to_admin(
    session_id: str,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    session = _get_owned_session(db, session_id, user_id)
    messages = db.query(ChatMessage).filter(ChatMessage.session_id == session_id)\
        .order_by(ChatMessage.timestamp.asc()).all()
    content = _build_admin_report_content(session, messages)

    report = db.query(AdminReport).filter(
        AdminReport.user_id == user_id,
        AdminReport.post_id == session_id,
    ).first()
    if report:
        report.content = content
        report.is_reviewed = False
        report.created_at = datetime.utcnow()
    else:
        db.add(AdminReport(
            user_id=user_id,
            post_id=session_id,
            content=content,
            is_reviewed=False,
        ))

    session.is_active = False
    if session.end_time is None:
        session.end_time = datetime.utcnow()
    db.commit()
    return {"message": "Sent to admin"}


@router.post("/messages", response_model=ChatMessageResponse)
def create_message(
    req: ChatMessageCreateRequest,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    _get_owned_session(db, req.session_id, user_id)
    existing = db.query(ChatMessage).filter(ChatMessage.id == req.id).first()
    if existing:
        if existing.user_id != user_id:
            raise HTTPException(status_code=403, detail="Cannot access this message")
        return ChatMessageResponse(
            id=existing.id,
            session_id=existing.session_id,
            user_id=existing.user_id,
            content=existing.content,
            is_from_user=existing.is_from_user,
            timestamp=existing.timestamp.isoformat(),
        )
    msg = ChatMessage(
        id=req.id,
        session_id=req.session_id,
        user_id=user_id,
        content=req.content,
        is_from_user=req.is_from_user,
        timestamp=datetime.utcnow(),
    )
    db.add(msg)
    db.commit()
    return ChatMessageResponse(
        id=msg.id,
        session_id=msg.session_id,
        user_id=msg.user_id,
        content=msg.content,
        is_from_user=msg.is_from_user,
        timestamp=msg.timestamp.isoformat(),
    )


@router.get("/sessions/{session_id}/messages", response_model=list[ChatMessageResponse])
def get_session_messages(
    session_id: str,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    _get_owned_session(db, session_id, user_id)
    msgs = db.query(ChatMessage).filter(ChatMessage.session_id == session_id)\
        .order_by(ChatMessage.timestamp.asc()).all()
    return [
        ChatMessageResponse(
            id=msg.id,
            session_id=msg.session_id,
            user_id=msg.user_id,
            content=msg.content,
            is_from_user=msg.is_from_user,
            timestamp=msg.timestamp.isoformat(),
        )
        for msg in msgs
    ]
