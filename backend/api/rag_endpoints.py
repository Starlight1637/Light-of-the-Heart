"""
RAG API Endpoints (FastAPI)
对应 Android RAGApiService.kt 的接口定义
"""
from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel, Field
from typing import Optional
from loguru import logger

from backend.rag import MindfulRAGService
from backend.auth import get_current_user_id

router = APIRouter(prefix="/api/rag", tags=["RAG"])

# 懒加载单例
_rag_service: Optional[MindfulRAGService] = None


def get_rag_service() -> MindfulRAGService:
    global _rag_service
    if _rag_service is None:
        _rag_service = MindfulRAGService()
    return _rag_service


# ----------------------------------------------------------------
# 请求/响应模型（与 Android RAGModels.kt 对应）
# ----------------------------------------------------------------

class RAGRetrieveRequest(BaseModel):
    query: str = Field(..., min_length=1, max_length=2000)
    risk_level: str = Field(default="LOW", pattern="^(LOW|MEDIUM|HIGH|CRITICAL)$")
    top_k: int = Field(default=5, ge=1, le=20)
    categories: Optional[list[str]] = None


class RAGRetrievedDoc(BaseModel):
    id: str
    title: str
    content: str
    category: str
    relevance_score: float
    source: str = "知识库"


class RAGRetrieveResponse(BaseModel):
    docs: list[RAGRetrievedDoc]
    query_rewritten: Optional[str] = None
    retrieval_strategy: str = "crag_self_rag"


class SimpleMessage(BaseModel):
    role: str  # "user" or "assistant"
    content: str


class RAGGenerateRequest(BaseModel):
    user_message: str = Field(..., min_length=1, max_length=2000)
    risk_level: str = Field(default="LOW", pattern="^(LOW|MEDIUM|HIGH|CRITICAL)$")
    retrieved_docs: list[RAGRetrievedDoc]
    session_history: Optional[list[SimpleMessage]] = None


class RAGGenerateResponse(BaseModel):
    message: str
    sources_used: list[str] = []
    confidence: float = 1.0
    self_rag_passed: bool = True


def _doc_to_response(doc: dict) -> RAGRetrievedDoc:
    return RAGRetrievedDoc(
        id=str(doc.get("id", "")),
        title=str(doc.get("title", "")),
        content=str(doc.get("content", "")),
        category=str(doc.get("category", "")),
        relevance_score=float(doc.get("relevance_score", 0.0)),
        source=str(doc.get("source", "knowledge_base")),
    )


# ----------------------------------------------------------------
# 路由
# ----------------------------------------------------------------

@router.post("/retrieve", response_model=RAGRetrieveResponse)
async def retrieve_knowledge(
    request: RAGRetrieveRequest,
    authorization: Optional[str] = Header(default=None),
):
    """检索心理健康知识库"""
    try:
        get_current_user_id(authorization)
        service = get_rag_service()
        docs = service.retrieve_knowledge(
            query=request.query,
            risk_level=request.risk_level,
            top_k=request.top_k
        )
        return RAGRetrieveResponse(
            docs=[_doc_to_response(d) for d in docs],
            retrieval_strategy="crag_self_rag"
        )
    except Exception as e:
        logger.error(f"Retrieve error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/generate", response_model=RAGGenerateResponse)
async def generate_response(
    request: RAGGenerateRequest,
    authorization: Optional[str] = Header(default=None),
):
    """基于检索结果生成支持性回复"""
    try:
        get_current_user_id(authorization)
        service = get_rag_service()
        history = None
        if request.session_history:
            history = [{"role": m.role, "content": m.content} for m in request.session_history]

        result = service.generate_response(
            user_message=request.user_message,
            risk_level=request.risk_level,
            retrieved_docs=[d.model_dump() for d in request.retrieved_docs],
            session_history=history
        )
        return RAGGenerateResponse(**result)
    except Exception as e:
        logger.error(f"Generate error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/health")
async def health_check():
    """健康检查"""
    try:
        service = get_rag_service()
        count = len(service.knowledge_base)
        return {"status": "ok", "knowledge_count": count, "bm25_ready": service.bm25 is not None}
    except Exception as e:
        return {"status": "error", "detail": str(e)}
