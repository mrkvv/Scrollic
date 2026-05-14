import redis.asyncio as redis
from fastapi import Header, HTTPException, status
import logging
from app.config import config

logger = logging.getLogger(__name__)

_redis_client = None


async def get_redis() -> redis.Redis:
    """Получение асинхронного Redis клиента"""
    global _redis_client

    if _redis_client is None:
        try:
            _redis_client = redis.Redis(
                host=config.REDIS_HOST,
                port=config.REDIS_PORT,
                decode_responses=True,
                socket_connect_timeout=3,
                socket_timeout=3
            )
            await _redis_client.ping()
            logger.info(f"Connected to Redis at {config.REDIS_HOST}:{config.REDIS_PORT}")
        except Exception as e:
            logger.warning(f"Failed to connect to Redis: {e}")
            _redis_client = None

    return _redis_client


async def get_current_user(
        x_user_id: int = Header(..., alias="X-User-Id")
) -> int:
    """Получение user_id из заголовка от Gateway"""
    if not x_user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing X-User-Id header"
        )

    logger.debug(f"Action requested for user: {x_user_id}")
    return x_user_id