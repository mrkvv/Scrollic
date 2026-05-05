import redis
from app.config import config
import logging

logger = logging.getLogger(__name__)

def get_redis_client():
    """
    Получение Redis клиента для других частей приложения
    """
    try:
        client = redis.Redis(
            host=config.REDIS_HOST,
            port=config.REDIS_PORT,
            decode_responses=True,
            socket_connect_timeout=3,
            socket_timeout=3
        )
        client.ping()
        logger.info(f"Redis client connected to {config.REDIS_HOST}:{config.REDIS_PORT}")
        return client
    except Exception as e:
        logger.error(f"Failed to create Redis client: {e}")
        return None