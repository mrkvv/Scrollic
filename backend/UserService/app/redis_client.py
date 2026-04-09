import redis
from .config import config

redis_client = redis.Redis(
    host=config.REDIS_HOST,
    port=config.REDIS_PORT,
    decode_responses=True
)


def get_redis():
    return redis_client