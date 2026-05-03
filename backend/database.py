"""
SQLite database configuration.
"""
import os
from pathlib import Path

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

DEFAULT_DB_PATH = Path(__file__).with_name("mindful.db")
DB_PATH = os.getenv("DB_PATH", str(DEFAULT_DB_PATH))
SQLALCHEMY_DATABASE_URL = f"sqlite:///{DB_PATH}"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


class Base(DeclarativeBase):
    pass


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def init_db():
    from backend.models import (
        AdminReport,
        ChatMessage,
        ChatSession,
        Feedback,
        FlaggedEntry,
        Post,
        User,
        WatchListEntry,
    )

    Base.metadata.create_all(bind=engine)
    _seed_default_accounts()


def _seed_default_accounts():
    from backend.auth import get_password_hash
    from backend.models import User

    db = SessionLocal()
    try:
        if db.query(User).count() == 0:
            db.add_all([
                User(
                    school="心光大学",
                    account="admin001",
                    password_hash=get_password_hash("admin123"),
                    role="admin",
                ),
                User(
                    school="心光大学",
                    account="student001",
                    password_hash=get_password_hash("student123"),
                    role="user",
                ),
            ])
            db.commit()
    finally:
        db.close()
