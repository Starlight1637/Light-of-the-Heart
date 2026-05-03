"""
心光完整后端服务（本地开发版）
运行: uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
"""
import os
from pathlib import Path
from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger

load_dotenv(Path(__file__).with_name(".env"))

from backend.database import init_db
from backend.api.rag_endpoints import router as rag_router
from backend.api.chat_endpoints import router as chat_router
from backend.api.auth_endpoints import router as auth_router
from backend.api.posts_endpoints import router as posts_router
from backend.api.session_endpoints import router as session_router
from backend.api.admin_endpoints import router as admin_router

app = FastAPI(
    title="心光后端服务（本地开发）",
    description="完整心理健康后端 + CRAG/Self-RAG 知识增强生成",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        origin.strip()
        for origin in os.getenv(
            "CORS_ALLOW_ORIGINS",
            "http://localhost:5173,http://127.0.0.1:5173,http://localhost:3000,http://127.0.0.1:3000",
        ).split(",")
        if origin.strip()
    ],
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册所有路由
app.include_router(auth_router)
app.include_router(posts_router)
app.include_router(session_router)
app.include_router(admin_router)
app.include_router(rag_router)
app.include_router(chat_router)


@app.on_event("startup")
def startup():
    logger.info("初始化数据库...")
    init_db()
    logger.info("数据库初始化完成，默认账号：admin001/admin123，student001/student123")


@app.get("/")
def root():
    return {"message": "心光后端服务运行中", "version": "2.0.0"}


if __name__ == "__main__":
    import uvicorn
    host = os.getenv("RAG_HOST", "0.0.0.0")
    port = int(os.getenv("RAG_PORT", "8000"))
    logger.info(f"启动服务: http://{host}:{port}")
    uvicorn.run("backend.main:app", host=host, port=port, reload=True)
