"""
JWT authentication helpers.
"""
import os
import warnings
from datetime import datetime, timedelta
from typing import Optional

from fastapi import Header, HTTPException, status
from jose import JWTError, jwt
from passlib.context import CryptContext

def _resolve_secret_key() -> str:
    configured = os.getenv("JWT_SECRET_KEY")
    if configured:
        return configured

    environment = os.getenv("ENVIRONMENT", "development").lower()
    require_secret = os.getenv("REQUIRE_JWT_SECRET", "false").lower() == "true"
    if environment in {"prod", "production"} or require_secret:
        raise RuntimeError("JWT_SECRET_KEY must be configured outside development")

    warnings.warn(
        "JWT_SECRET_KEY is not configured; using a development-only fallback. "
        "Set JWT_SECRET_KEY before deployment.",
        RuntimeWarning,
        stacklevel=2,
    )
    return "dev-only-change-me-before-deployment"


SECRET_KEY = _resolve_secret_key()
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_HOURS = 24 * 7

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain: str, hashed: str) -> bool:
    return pwd_context.verify(plain, hashed)


def create_access_token(user_id: int, role: str) -> str:
    expire = datetime.utcnow() + timedelta(hours=ACCESS_TOKEN_EXPIRE_HOURS)
    payload = {"sub": str(user_id), "role": role, "exp": expire}
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def decode_token(token: str) -> Optional[dict]:
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError:
        return None


def _get_payload_from_authorization(authorization: Optional[str]) -> dict:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing authorization token",
        )
    token = authorization.removeprefix("Bearer ")
    payload = decode_token(token)
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authorization token",
        )
    return payload


def get_current_user_id(authorization: Optional[str]) -> int:
    payload = _get_payload_from_authorization(authorization)
    try:
        return int(payload["sub"])
    except (KeyError, TypeError, ValueError):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authorization token",
        )


def get_current_role(authorization: Optional[str]) -> str:
    payload = _get_payload_from_authorization(authorization)
    return payload.get("role", "user")


def require_admin(authorization: Optional[str] = Header(default=None)) -> dict:
    payload = _get_payload_from_authorization(authorization)
    if payload.get("role") != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Admin access required",
        )
    return payload
