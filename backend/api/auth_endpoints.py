"""
Authentication endpoints.
"""
from typing import Optional

from fastapi import APIRouter, Depends, Header, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from backend.auth import create_access_token, get_current_user_id, get_password_hash, verify_password
from backend.database import get_db
from backend.models import User

router = APIRouter(tags=["Auth"])

SCHOOLS = ["心光大学"]


class LoginRequest(BaseModel):
    school: str
    account: str
    password: str


class LoginResponse(BaseModel):
    token: str
    user_id: int
    school: str
    account: str
    role: str
    message: str


class ChangePasswordRequest(BaseModel):
    old_password: str
    new_password: str


class MessageResponse(BaseModel):
    message: str


class SchoolsResponse(BaseModel):
    schools: list[str]


@router.get("/schools", response_model=SchoolsResponse)
def get_schools():
    return SchoolsResponse(schools=SCHOOLS)


@router.post("/auth/login", response_model=LoginResponse)
def login(req: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(
        User.school == req.school,
        User.account == req.account,
    ).first()

    if not user or not verify_password(req.password, user.password_hash):
        raise HTTPException(status_code=401, detail="账号或密码错误")

    token = create_access_token(user.id, user.role)
    return LoginResponse(
        token=token,
        user_id=user.id,
        school=user.school,
        account=user.account,
        role=user.role,
        message="登录成功",
    )


@router.post("/auth/change-password", response_model=MessageResponse)
def change_password(
    req: ChangePasswordRequest,
    authorization: Optional[str] = Header(default=None),
    db: Session = Depends(get_db),
):
    user_id = get_current_user_id(authorization)
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="用户不存在")
    if not verify_password(req.old_password, user.password_hash):
        raise HTTPException(status_code=401, detail="原密码错误")
    user.password_hash = get_password_hash(req.new_password)
    db.commit()
    return MessageResponse(message="密码修改成功")
