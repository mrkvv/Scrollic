import logging
from fastapi import APIRouter, Depends, Query
from app.schemas import FeedResponse
from app.dependencies import get_current_user
from app.database import get_cassandra
from app.services.feed_service import FeedService
from app.config import config

router = APIRouter(prefix="/api/feed", tags=["feed"])
logger = logging.getLogger(__name__)


@router.get("", response_model=FeedResponse)
async def get_feed(
        limit: int = Query(default=config.DEFAULT_LIMIT, ge=1, le=config.MAX_LIMIT),
        user_id: int = Depends(get_current_user),
        cassandra_session=Depends(get_cassandra)
):
    """
    Получение персонализированной ленты новостей из Cassandra.
    user_id получается из заголовка X-User-Id, который добавляет Gateway.
    """
    feed_items = []
    try:
        if cassandra_session:
            feed_service = FeedService(cassandra_session)
            feed_items = await feed_service.get_personalized_feed(user_id, limit)
            logger.info(f"Got {len(feed_items)} items from Cassandra for user {user_id}")
        else:
            logger.warning("Cassandra not available, returning empty feed")
    except Exception as e:
        logger.error(f"Error getting feed: {e}")

    response = FeedResponse(feed=feed_items, total=len(feed_items), limit=limit)

    return response