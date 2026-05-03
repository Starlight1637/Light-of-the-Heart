"""
Admin endpoints.
"""
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from backend.auth import get_password_hash, require_admin
from backend.database import get_db
from backend.models import AdminReport, Feedback, FlaggedEntry, User, WatchListEntry

router = APIRouter(
    prefix="/admin",
    tags=["Admin"],
    dependencies=[Depends(require_admin)],
)


class WatchListItemResponse(BaseModel):
    user_id: int
    userId: int
    account: str
    school: str
    latest_risk_level: str
    riskLevel: str
    is_handled: bool
    status: str
    flaggedAt: str
    latestKeywords: list[str] = []


class FlaggedEntryResponse(BaseModel):
    id: int
    entryId: str
    post_id: Optional[str]
    risk_level: str
    riskLevel: str
    content_preview: str
    content: str
    riskKeywords: list[str] = []
    flagged_at: str
    timestamp: str


class AdminReportResponse(BaseModel):
    id: str
    user_id: int
    userId: int
    account: str
    school: str
    post_id: Optional[str]
    content: str
    moodSummary: str
    riskIndicators: list[str] = []
    is_reviewed: bool
    isReviewed: bool
    created_at: str
    reportDate: str


class FeedbackItemResponse(BaseModel):
    id: str
    user_id: int
    userId: int
    account: str
    school: str
    content: str
    created_at: str
    createdAt: str
    is_read: bool
    status: str


class BatchAccountRequest(BaseModel):
    school: str
    accounts: Optional[list[str]] = None
    default_password: str = "mindful123"
    accountStart: Optional[str] = None
    accountEnd: Optional[str] = None
    role: str = "user"


class BatchAccountResponse(BaseModel):
    created: int
    skipped: int
    totalRequested: int
    successCount: int
    failedCount: int
    skippedAccounts: list[str]
    accounts: list[dict[str, str]]
    message: str


class MessageResponse(BaseModel):
    message: str


class FeedbackStatusRequest(BaseModel):
    status: str = "resolved"


def _risk_to_client(level: str) -> str:
    normalized = (level or "LOW").upper()
    if normalized == "CRITICAL":
        return "critical"
    if normalized == "HIGH":
        return "high"
    if normalized == "MEDIUM":
        return "medium"
    return "low"


def _resolve_user(db: Session, user_id: int) -> User:
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


def _expand_account_range(start: str, end: str) -> list[str]:
    import re

    start_match = re.match(r"^(.*?)(\d+)$", start or "")
    end_match = re.match(r"^(.*?)(\d+)$", end or "")
    if not start_match or not end_match or start_match.group(1) != end_match.group(1):
        raise HTTPException(status_code=400, detail="Invalid account range")

    prefix = start_match.group(1)
    start_num = int(start_match.group(2))
    end_num = int(end_match.group(2))
    width = max(len(start_match.group(2)), len(end_match.group(2)))
    if end_num < start_num:
        raise HTTPException(status_code=400, detail="Invalid account range")
    return [f"{prefix}{i:0{width}d}" for i in range(start_num, end_num + 1)]


@router.get("/watchlist", response_model=list[WatchListItemResponse])
def get_watchlist(db: Session = Depends(get_db)):
    entries = db.query(WatchListEntry).filter(WatchListEntry.is_handled == False).all()
    result: list[WatchListItemResponse] = []
    for entry in entries:
        user = db.query(User).filter(User.id == entry.user_id).first()
        if not user:
            continue
        result.append(WatchListItemResponse(
            user_id=entry.user_id,
            userId=entry.user_id,
            account=user.account,
            school=user.school,
            latest_risk_level=entry.latest_risk_level,
            riskLevel=_risk_to_client(entry.latest_risk_level),
            is_handled=entry.is_handled,
            status="handled" if entry.is_handled else "active",
            flaggedAt=entry.updated_at.isoformat(),
            latestKeywords=[],
        ))
    return result


@router.get("/watchlist/{user_id}/entries", response_model=list[FlaggedEntryResponse])
def get_flagged_entries(user_id: int, db: Session = Depends(get_db)):
    _resolve_user(db, user_id)
    entries = db.query(FlaggedEntry).filter(FlaggedEntry.user_id == user_id)\
        .order_by(FlaggedEntry.flagged_at.desc()).all()
    return [
        FlaggedEntryResponse(
            id=entry.id,
            entryId=str(entry.id),
            post_id=entry.post_id,
            risk_level=entry.risk_level,
            riskLevel=_risk_to_client(entry.risk_level),
            content_preview=entry.content_preview,
            content=entry.content_preview,
            riskKeywords=[],
            flagged_at=entry.flagged_at.isoformat(),
            timestamp=entry.flagged_at.isoformat(),
        )
        for entry in entries
    ]


@router.put("/watchlist/{user_id}/handle", response_model=MessageResponse)
def mark_as_handled(user_id: int, db: Session = Depends(get_db)):
    entry = db.query(WatchListEntry).filter(WatchListEntry.user_id == user_id).first()
    if not entry:
        raise HTTPException(status_code=404, detail="Watch list entry not found")
    entry.is_handled = True
    db.commit()
    return MessageResponse(message="已标记为已处理")


@router.get("/reports", response_model=list[AdminReportResponse])
def get_reports(db: Session = Depends(get_db)):
    reports = db.query(AdminReport).order_by(AdminReport.created_at.desc()).all()
    result: list[AdminReportResponse] = []
    for report in reports:
        user = db.query(User).filter(User.id == report.user_id).first()
        result.append(AdminReportResponse(
            id=str(report.id),
            user_id=report.user_id,
            userId=report.user_id,
            account=user.account if user else f"user-{report.user_id}",
            school=user.school if user else "",
            post_id=report.post_id,
            content=report.content,
            moodSummary=report.content,
            riskIndicators=[],
            is_reviewed=report.is_reviewed,
            isReviewed=report.is_reviewed,
            created_at=report.created_at.isoformat(),
            reportDate=report.created_at.isoformat(),
        ))
    return result


@router.get("/reports/{report_id}", response_model=AdminReportResponse)
def get_report_detail(report_id: int, db: Session = Depends(get_db)):
    report = db.query(AdminReport).filter(AdminReport.id == report_id).first()
    if not report:
        raise HTTPException(status_code=404, detail="Report not found")
    user = db.query(User).filter(User.id == report.user_id).first()
    return AdminReportResponse(
        id=str(report.id),
        user_id=report.user_id,
        userId=report.user_id,
        account=user.account if user else f"user-{report.user_id}",
        school=user.school if user else "",
        post_id=report.post_id,
        content=report.content,
        moodSummary=report.content,
        riskIndicators=[],
        is_reviewed=report.is_reviewed,
        isReviewed=report.is_reviewed,
        created_at=report.created_at.isoformat(),
        reportDate=report.created_at.isoformat(),
    )


@router.put("/reports/{report_id}/review", response_model=MessageResponse)
def mark_report_reviewed(report_id: int, db: Session = Depends(get_db)):
    report = db.query(AdminReport).filter(AdminReport.id == report_id).first()
    if not report:
        raise HTTPException(status_code=404, detail="Report not found")
    report.is_reviewed = True
    db.commit()
    return MessageResponse(message="已标记为已审核")


@router.post("/accounts/batch", response_model=BatchAccountResponse)
def create_batch_accounts(req: BatchAccountRequest, db: Session = Depends(get_db)):
    if req.accounts is not None:
        account_names = [account.strip() for account in req.accounts if account.strip()]
    elif req.accountStart and req.accountEnd:
        account_names = _expand_account_range(req.accountStart, req.accountEnd)
    else:
        raise HTTPException(status_code=400, detail="No accounts provided")

    created_accounts: list[dict[str, str]] = []
    skipped_accounts: list[str] = []
    for account_name in account_names:
        existing = db.query(User).filter(
            User.school == req.school,
            User.account == account_name,
        ).first()
        if existing:
            skipped_accounts.append(account_name)
            continue
        db.add(User(
            school=req.school,
            account=account_name,
            password_hash=get_password_hash(req.default_password),
            role=req.role if req.role in ("admin", "user") else "user",
        ))
        created_accounts.append({
            "account": account_name,
            "password": req.default_password,
        })

    db.commit()
    created = len(created_accounts)
    skipped = len(skipped_accounts)
    total = len(account_names)
    return BatchAccountResponse(
        created=created,
        skipped=skipped,
        totalRequested=total,
        successCount=created,
        failedCount=skipped,
        skippedAccounts=skipped_accounts,
        accounts=created_accounts,
        message=f"成功创建 {created} 个账号，跳过 {skipped} 个已存在账号",
    )


@router.get("/feedback", response_model=list[FeedbackItemResponse])
def get_feedback(db: Session = Depends(get_db)):
    items = db.query(Feedback).order_by(Feedback.created_at.desc()).all()
    result: list[FeedbackItemResponse] = []
    for item in items:
        user = db.query(User).filter(User.id == item.user_id).first()
        status = "resolved" if item.is_read else "pending"
        result.append(FeedbackItemResponse(
            id=str(item.id),
            user_id=item.user_id,
            userId=item.user_id,
            account=user.account if user else f"user-{item.user_id}",
            school=user.school if user else "",
            content=item.content,
            created_at=item.created_at.isoformat(),
            createdAt=item.created_at.isoformat(),
            is_read=item.is_read,
            status=status,
        ))
    return result


@router.patch("/feedback/{feedback_id}", response_model=MessageResponse)
def update_feedback_status(
    feedback_id: int,
    req: FeedbackStatusRequest,
    db: Session = Depends(get_db),
):
    item = db.query(Feedback).filter(Feedback.id == feedback_id).first()
    if not item:
        raise HTTPException(status_code=404, detail="Feedback not found")
    item.is_read = req.status == "resolved"
    db.commit()
    return MessageResponse(message="反馈状态已更新")
