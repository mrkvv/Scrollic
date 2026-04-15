import redis
from .config import config
import logging

logger = logging.getLogger(__name__)

try:
    redis_client = redis.Redis(
        host=config.REDIS_HOST,
        port=config.REDIS_PORT,
        decode_responses=True,
        socket_connect_timeout=2
    )
    redis_client.ping()
    logger.info("Redis connected successfully")
except Exception as e:
    logger.warning(f"Redis connection failed: {e}. Running without Redis.")
    redis_client = None


def get_redis():
    return redis_client