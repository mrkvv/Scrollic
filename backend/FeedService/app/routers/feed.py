import logging
from fastapi import APIRouter, Depends, Query
import redis
from app.schemas import FeedResponse
from app.dependencies import get_current_user, get_redis
from app.database import get_cassandra
from app.services.feed_service import FeedService
from app.config import config

router = APIRouter(prefix="/api/feed", tags=["feed"])
logger = logging.getLogger(__name__)


@router.get("", response_model=FeedResponse)
async def get_feed(
        limit: int = Query(default=config.DEFAULT_LIMIT, ge=1, le=config.MAX_LIMIT),
        user_id: int = Depends(get_current_user),
        redis_client: redis.Redis = Depends(get_redis),
        cassandra_session=Depends(get_cassandra)
):
    """Получение персонализированной ленты новостей из Cassandra"""

    cache_key = f"feed:{user_id}:limit:{limit}"

    if redis_client:
        cached_data = redis_client.get(cache_key)
        if cached_data:
            logger.info(f"Cache HIT for user {user_id}")
            return FeedResponse.parse_raw(cached_data)
        logger.info(f"Cache MISS for user {user_id}")

    if not cassandra_session:
        logger.warning("Cassandra not available, using mock data")
        from app.routers.feed_mock import get_mock_feed
        mock_feed = get_mock_feed(limit)
        response = FeedResponse(feed=mock_feed, total=len(mock_feed), limit=limit)

        if redis_client:
            redis_client.setex(cache_key, config.CACHE_TTL_SECONDS, response.json())
        return response

    feed_service = FeedService(cassandra_session)
    feed_items = await feed_service.get_personalized_feed(user_id, limit)

    response = FeedResponse(feed=feed_items, total=len(feed_items), limit=limit)

    if redis_client:
        redis_client.setex(cache_key, config.CACHE_TTL_SECONDS, response.json())
        logger.info(f"💾 Cached feed for user {user_id}")

    return response