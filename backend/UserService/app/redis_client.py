import redis
from .config import config
import logging
import json

logger = logging.getLogger(__name__)

# Пытаемся подключиться к Redis
try:
    redis_client = redis.Redis(
        host=config.REDIS_HOST,
        port=config.REDIS_PORT,
        db=config.REDIS_DB,
        password=config.REDIS_PASSWORD if config.REDIS_PASSWORD else None,
        decode_responses=True,
        socket_connect_timeout=2
    )
    # Проверяем соединение
    redis_client.ping()
    logger.info("Redis connected successfully")
except Exception as e:
    logger.warning(f"Redis connection failed: {e}. Running without Redis.")
    redis_client = None


def get_redis():
    """Dependency для получения Redis клиента"""
    yield redis_client