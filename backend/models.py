"""
SQLAlchemy ORM 模型（SQLite，本地开发）
"""
from datetime import datetime
from sqlalchemy import (
    Column, Integer, String, Float, Boolean,
    DateTime, ForeignKey, Text
)
from backend.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, autoincrement=True)
    school = Column(String(100), nullable=False)
    account = Column(String(50), nullable=False)
    password_hash = Column(String(200), nullable=False)
    role = Column(String(20), default="user")          # "user" / "admin"
    created_at = Column(DateTime, default=datetime.utcnow)


class Post(Base):
    __tablename__ = "posts"

    id = Column(String(64), primary_key=True)          # UUID from Android
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    content = Column(Text, nullable=False)
    emotion_score = Column(Float, nullable=True)
    risk_level = Column(String(20), default="LOW")
    ai_response = Column(Text, nullable=True)
    is_anonymous = Column(Boolean, default=True)
    like_count = Column(Integer, default=0)
    comment_count = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)


class ChatSession(Base):
    __tablename__ = "chat_sessions"

    id = Column(String(64), primary_key=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    start_time = Column(DateTime, default=datetime.utcnow)
    end_time = Column(DateTime, nullable=True)
    mood_report = Column(Text, nullable=True)
    is_active = Column(Boolean, default=True)


class ChatMessage(Base):
    __tablename__ = "chat_messages"

    id = Column(String(64), primary_key=True)
    session_id = Column(String(64), ForeignKey("chat_sessions.id"), nullable=False)
    user_id = Column(Integer, nullable=False)
    content = Column(Text, nullable=False)
    is_from_user = Column(Boolean, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)


class Feedback(Base):
    __tablename__ = "feedback"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    content = Column(Text, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    is_read = Column(Boolean, default=False)


class AdminReport(Base):
    __tablename__ = "admin_reports"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    post_id = Column(String(64), nullable=True)
    content = Column(Text, nullable=False)
    is_reviewed = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)


class WatchListEntry(Base):
    __tablename__ = "watchlist"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), unique=True, nullable=False)
    latest_risk_level = Column(String(20), default="HIGH")
    is_handled = Column(Boolean, default=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class FlaggedEntry(Base):
    __tablename__ = "flagged_entries"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    post_id = Column(String(64), nullable=True)
    risk_level = Column(String(20), nullable=False)
    content_preview = Column(String(500), nullable=False)
    flagged_at = Column(DateTime, default=datetime.utcnow)
