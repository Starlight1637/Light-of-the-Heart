"""
聊天补全代理端点
将 Android 端的 /api/chat/completions 请求转发给 DeepSeek API
API Key 存储在后端环境变量中，不再暴露在 APK 里
"""
import os
from fastapi import APIRouter, Header, HTTPException
from pydantic import BaseModel, Field
from typing import Optional
from loguru import logger
from openai import OpenAI

from backend.auth import get_current_user_id


router = APIRouter(prefix="/api/chat", tags=["Chat"])


def get_client() -> OpenAI:
    api_key = os.getenv("DEEPSEEK_API_KEY")
    if not api_key:
        raise HTTPException(
            status_code=503,
            detail="DEEPSEEK_API_KEY is not configured on the backend",
        )
    return OpenAI(
        api_key=api_key,
        base_url="https://api.deepseek.com/v1"
    )


# ----------------------------------------------------------------
# 请求/响应模型（与 Android ZhipuAIService.kt 字段完全对应）
# ----------------------------------------------------------------

class ChatMessage(BaseModel):
    role: str  # "system" / "user" / "assistant"
    content: str


class ChatCompletionRequest(BaseModel):
    model: str = "deepseek-chat"
    messages: list[ChatMessage]
    temperature: float = Field(default=0.7, ge=0.0, le=2.0)
    top_p: float = Field(default=0.9, ge=0.0, le=1.0)
    max_tokens: int = Field(default=1024, ge=1, le=4000)


class ChatChoice(BaseModel):
    index: int
    message: ChatMessage
    finish_reason: Optional[str] = None


class ChatUsage(BaseModel):
    prompt_tokens: int
    completion_tokens: int
    total_tokens: int


class ChatCompletionResponse(BaseModel):
    id: str
    created: int
    model: str
    choices: list[ChatChoice]
    usage: Optional[ChatUsage] = None


# ----------------------------------------------------------------
# 路由
# ----------------------------------------------------------------

@router.post("/completions", response_model=ChatCompletionResponse)
async def chat_completions(
    request: ChatCompletionRequest,
    authorization: Optional[str] = Header(default=None),
):
    """
    聊天补全代理：接收 Android 请求 → 转发 DeepSeek → 返回结果
    API Key 由后端环境变量 DEEPSEEK_API_KEY 管理
    """
    try:
        get_current_user_id(authorization)
        client = get_client()
        response = client.chat.completions.create(
            model=request.model,
            messages=[{"role": m.role, "content": m.content} for m in request.messages],
            temperature=request.temperature,
            top_p=request.top_p,
            max_tokens=request.max_tokens
        )
        choice = response.choices[0]
        return ChatCompletionResponse(
            id=response.id,
            created=response.created,
            model=response.model,
            choices=[ChatChoice(
                index=choice.index,
                message=ChatMessage(role=choice.message.role, content=choice.message.content),
                finish_reason=choice.finish_reason
            )],
            usage=ChatUsage(
                prompt_tokens=response.usage.prompt_tokens,
                completion_tokens=response.usage.completion_tokens,
                total_tokens=response.usage.total_tokens
            ) if response.usage else None
        )
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Chat completion proxy error: {e}")
        raise HTTPException(status_code=500, detail=str(e))
