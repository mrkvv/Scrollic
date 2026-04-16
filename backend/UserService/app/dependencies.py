from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from .database import get_db
from .models import User
from .redis_client import get_redis
import redis

security = HTTPBearer()


async def get_current_user_id(
        credentials: HTTPAuthorizationCredentials = Depends(security),
        redis_client: redis.Redis = Depends(get_redis),
        db: Session = Depends(get_db)
) -> int:
    """
    Извлекает user_id из токена и проверяет наличие сессии в Redis.
    """
    token = credentials.credentials

    # 1. Проверяем наличие Redis
    if not redis_client:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Redis not available"
        )

    session_key = f"session:{token}"

    session_data = redis_client.hgetall(session_key)

    if not session_data:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Session not found or expired",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # 3. Извлекаем user_id из Hash (ключи в bytes)
    user_id_bytes = session_data.get(b"user_id")
    if not user_id_bytes:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid session data: missing user_id",
        )

    user_id = int(user_id_bytes)

    # 4. Проверяем, что пользователь существует в БД
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found",
        )

    return user_id