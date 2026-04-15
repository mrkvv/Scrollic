from fastapi import Depends, HTTPException, status, Header
from sqlalchemy.orm import Session
from .database import get_db
from .models import User


async def get_current_user_id(
    x_user_id: int = Header(..., alias="X-User-Id"),
    db: Session = Depends(get_db)
) -> int:
    """
    Получает user_id из заголовка X-User-Id от API Gateway.
    API Gateway уже проверил токен и подставил этот заголовок.
    """
    user = db.query(User).filter(User.id == x_user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found"
        )
    return x_user_id


async def get_current_user_optional(
    x_user_id: int = Header(None, alias="X-User-Id"),
    db: Session = Depends(get_db)
) -> int | None:
    """
    Опциональное получение user_id (для эндпоинтов где не обязателен)
    """
    if x_user_id:
        user = db.query(User).filter(User.id == x_user_id).first()
        return x_user_id if user else None
    return None


async def get_current_username(
    x_username: str = Header(..., alias="X-Username"),
    db: Session = Depends(get_db)
) -> str:
    """
    Получает username из заголовка X-Username от API Gateway
    """
    user = db.query(User).filter(User.name == x_username).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found"
        )
    return x_username