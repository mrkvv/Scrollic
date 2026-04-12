from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from .auth import validate_token, extract_user_id_from_token
from .redis_client import get_redis
import redis
import json

security = HTTPBearer()


async def get_current_user_id(
        credentials: HTTPAuthorizationCredentials = Depends(security),
        redis_client: redis.Redis = Depends(get_redis)
) -> int:
    token = credentials.credentials

    # Проверяем blacklist
    if redis_client:
        token_key = f"blacklist:{token}"
        if redis_client.exists(token_key):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token has been revoked",
                headers={"WWW-Authenticate": "Bearer"},
            )

    if not validate_token(token):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        )

    user_id = extract_user_id_from_token(token)
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token payload",
        )

    return user_id


async def get_current_user_optional(
        credentials: HTTPAuthorizationCredentials = Depends(security),
        redis_client: redis.Redis = Depends(get_redis)
) -> int | None:
    token = credentials.credentials

    if redis_client:
        token_key = f"blacklist:{token}"
        if redis_client.exists(token_key):
            return None

    if validate_token(token):
        return extract_user_id_from_token(token)
    return None


async def get_user_session(
        credentials: HTTPAuthorizationCredentials = Depends(security),
        redis_client: redis.Redis = Depends(get_redis)
) -> dict | None:
    """Получить данные сессии пользователя (если нужно)"""
    token = credentials.credentials

    if not redis_client:
        return None

    # Здесь можно по токену найти сессию
    # Для простоты возвращаем None
    return None