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


def get_mock_feed(limit: int):
    """Заглушка для моковых данных (на случай ошибки или отсутствия Cassandra)"""
    from uuid import uuid4
    from datetime import datetime, timedelta
    from app.schemas import NewsItem

    mock_news = []
    for i in range(min(limit, 50)):
        mock_news.append(NewsItem(
            id=uuid4(),
            head=f"Новость {i + 1}: Заглушка FeedService",
            summary=f"Краткое описание новости {i + 1}",
            text=f"Полный текст новости {i + 1}",
            url=f"https://example.com/news/{i + 1}",
            url_picture=f"https://example.com/images/{i + 1}.jpg",
            popularity=1000 - i * 50,
            theme_id=(i % 5) + 1,
            created_at=datetime.now() - timedelta(days=i)
        ))
    return mock_news


@router.get("", response_model=FeedResponse)
async def get_feed(
        limit: int = Query(default=config.DEFAULT_LIMIT, ge=1, le=config.MAX_LIMIT),
        user_id: int = Depends(get_current_user),
        redis_client: redis.Redis = Depends(get_redis),
        cassandra_session=Depends(get_cassandra)
):
    """
    Получение персонализированной ленты новостей из Cassandra.
    user_id получается из заголовка X-User-Id, который добавляет Gateway.
    """
    cache_key = f"feed:{user_id}:limit:{limit}"

    # 1. Проверяем кэш Redis
    if redis_client:
        try:
            cached_data = redis_client.get(cache_key)
            if cached_data:
                logger.info(f"Cache HIT for user {user_id}")
                return FeedResponse.parse_raw(cached_data)
        except Exception as e:
            logger.error(f"Redis get error: {e}")

        logger.info(f"Cache MISS for user {user_id}")

    # 2. Получаем данные из Cassandra или моков
    feed_items = []
    try:
        if cassandra_session:
            feed_service = FeedService(cassandra_session)
            feed_items = await feed_service.get_personalized_feed(user_id, limit)
            logger.info(f"Got {len(feed_items)} items from Cassandra for user {user_id}")
        else:
            logger.warning("Cassandra not available, using mock data")
            feed_items = get_mock_feed(limit)
    except Exception as e:
        logger.error(f"Error getting feed: {e}")
        logger.warning("Falling back to mock data")
        feed_items = get_mock_feed(limit)

    response = FeedResponse(feed=feed_items, total=len(feed_items), limit=limit)

    # 3. Сохраняем в кэш
    if redis_client:
        try:
            redis_client.setex(cache_key, config.CACHE_TTL_SECONDS, response.json())
            logger.info(f"💾 Cached feed for user {user_id} (TTL={config.CACHE_TTL_SECONDS}s)")
        except Exception as e:
            logger.error(f"Redis set error: {e}")

    return response