import logging
import json
import asyncio
from fastapi import APIRouter, Depends, HTTPException
from uuid import UUID
from datetime import datetime
from app.schemas import (
    BatchActions, BatchResponse, ActionResponse,
    ActionStatusResponse
)
from app.dependencies import get_current_user, get_redis
from app.database import get_cassandra
from app.services.weight_recalculator import WeightRecalculator

router = APIRouter(prefix="/api/actions", tags=["actions"])
logger = logging.getLogger(__name__)


async def recalculate_user_weights_async(user_id: int, cassandra_session):
    """Фоновая задача для пересчета весов"""
    try:
        recalculator = WeightRecalculator(cassandra_session)
        await recalculator.update_user_weights(user_id)
        logger.info(f"Background weight recalculation completed for user {user_id}")
    except Exception as e:
        logger.error(f"Background weight recalculation failed for user {user_id}: {e}")


@router.post("/like", response_model=ActionResponse)
async def like_news(
        news_id: UUID,
        user_id: int = Depends(get_current_user),
        cassandra_session=Depends(get_cassandra),
        redis_client=Depends(get_redis)
):
    """Поставить лайк - запись в Cassandra и инвалидация кэша"""
    try:
        if not cassandra_session:
            logger.warning("Cassandra not available, using mock")
            return ActionResponse(
                status="accepted",
                message="Like accepted (mock - no cassandra)",
                news_id=news_id
            )

        # Записываем лайк в таблицу user_likes
        query = """
            INSERT INTO user_likes (user_id, news_id, liked_at)
            VALUES (%s, %s, %s)
        """
        cassandra_session.execute(query, (user_id, news_id, datetime.now()))

        # Инвалидируем кэш статуса
        cache_key = f"action_status:{user_id}:{news_id}"
        if redis_client:
            redis_client.delete(cache_key)
            logger.info(f"Invalidated cache for key: {cache_key}")

        logger.info(f"User {user_id} liked news {news_id}")

        # Запускаем пересчет весов в фоне (не блокируем ответ)
        asyncio.create_task(recalculate_user_weights_async(user_id, cassandra_session))

        return ActionResponse(
            status="accepted",
            message="Like recorded successfully",
            news_id=news_id
        )

    except Exception as e:
        logger.error(f"Error saving like: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/like", response_model=ActionResponse)
async def unlike_news(
        news_id: UUID,
        user_id: int = Depends(get_current_user),
        cassandra_session=Depends(get_cassandra),
        redis_client=Depends(get_redis)
):
    """Удалить лайк"""
    try:
        if not cassandra_session:
            logger.warning("Cassandra not available, using mock")
            return ActionResponse(
                status="accepted",
                message="Unlike accepted (mock - no cassandra)",
                news_id=news_id
            )

        # Удаляем лайк из таблицы user_likes
        query = "DELETE FROM user_likes WHERE user_id = %s AND news_id = %s"
        cassandra_session.execute(query, (user_id, news_id))

        # Инвалидируем кэш статуса
        cache_key = f"action_status:{user_id}:{news_id}"
        if redis_client:
            redis_client.delete(cache_key)
            logger.info(f"Invalidated cache for key: {cache_key}")

        logger.info(f"User {user_id} unliked news {news_id}")

        # Запускаем пересчет весов в фоне (не блокируем ответ)
        asyncio.create_task(recalculate_user_weights_async(user_id, cassandra_session))

        return ActionResponse(
            status="accepted",
            message="Unlike recorded successfully",
            news_id=news_id
        )

    except Exception as e:
        logger.error(f"Error deleting like: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/seen", response_model=ActionResponse)
async def mark_as_seen(
        news_id: UUID,
        user_id: int = Depends(get_current_user),
        cassandra_session=Depends(get_cassandra),
        redis_client=Depends(get_redis)
):
    """Отметить новость как просмотренную"""
    try:
        if not cassandra_session:
            logger.warning("Cassandra not available, using mock")
            return ActionResponse(
                status="accepted",
                message="Seen accepted (mock - no cassandra)",
                news_id=news_id
            )

        # Записываем просмотр в таблицу user_seen (TTL 7 дней)
        query = """
            INSERT INTO user_seen (user_id, news_id, seen_at)
            VALUES (%s, %s, %s)
        """
        cassandra_session.execute(query, (user_id, news_id, datetime.now()))

        # Инвалидируем кэш статуса
        cache_key = f"action_status:{user_id}:{news_id}"
        if redis_client:
            redis_client.delete(cache_key)
            logger.info(f"Invalidated cache for key: {cache_key}")

        logger.info(f"User {user_id} seen news {news_id}")

        # Запускаем пересчет весов в фоне (не блокируем ответ)
        asyncio.create_task(recalculate_user_weights_async(user_id, cassandra_session))

        return ActionResponse(
            status="accepted",
            message="Seen recorded successfully",
            news_id=news_id
        )

    except Exception as e:
        logger.error(f"Error saving seen: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/status/{news_id}", response_model=ActionStatusResponse)
async def get_action_status(
        news_id: UUID,
        user_id: int = Depends(get_current_user),
        cassandra_session=Depends(get_cassandra),
        redis_client=Depends(get_redis)
):
    """Получить статус действий (лайкнул/посмотрел) с кэшированием"""
    cache_key = f"action_status:{user_id}:{news_id}"

    try:
        # 1. Пытаемся получить из Redis кэша
        if redis_client:
            cached_data = redis_client.get(cache_key)
            if cached_data:
                cached = json.loads(cached_data)
                logger.info(f"Cache HIT for {cache_key}")
                return ActionStatusResponse(
                    news_id=news_id,
                    liked=cached['liked'],
                    seen=cached['seen']
                )

        if not cassandra_session:
            logger.warning("Cassandra not available, returning default false")
            return ActionStatusResponse(
                news_id=news_id,
                liked=False,
                seen=False
            )

        # 2. Проверяем лайк
        like_query = "SELECT news_id FROM user_likes WHERE user_id = %s AND news_id = %s"
        like_result = cassandra_session.execute(like_query, (user_id, news_id))
        liked = len(list(like_result)) > 0

        # 3. Проверяем просмотр
        seen_query = "SELECT news_id FROM user_seen WHERE user_id = %s AND news_id = %s"
        seen_result = cassandra_session.execute(seen_query, (user_id, news_id))
        seen = len(list(seen_result)) > 0

        logger.info(f"Status for user {user_id}, news {news_id}: liked={liked}, seen={seen}")

        # 4. Сохраняем в кэш (TTL 60 секунд)
        if redis_client:
            cache_data = {
                "liked": liked,
                "seen": seen
            }
            redis_client.setex(cache_key, 60, json.dumps(cache_data))
            logger.info(f"Cached status for {cache_key} (TTL=60s)")

        return ActionStatusResponse(
            news_id=news_id,
            liked=liked,
            seen=seen
        )

    except Exception as e:
        logger.error(f"Error getting status: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/batch", response_model=BatchResponse)
async def batch_actions(
        batch: BatchActions,
        user_id: int = Depends(get_current_user),
        cassandra_session=Depends(get_cassandra),
        redis_client=Depends(get_redis)
):
    """Обработка пачки действий с инвалидацией кэша"""
    try:
        processed = 0

        for action in batch.actions:
            if action.action == "like":
                # Сохраняем лайк
                query = """
                    INSERT INTO user_likes (user_id, news_id, liked_at)
                    VALUES (%s, %s, %s)
                """
                cassandra_session.execute(query, (user_id, action.news_id, datetime.now()))
                processed += 1

                # Инвалидируем кэш
                if redis_client:
                    cache_key = f"action_status:{user_id}:{action.news_id}"
                    redis_client.delete(cache_key)

            elif action.action == "seen":
                # Сохраняем просмотр
                query = """
                    INSERT INTO user_seen (user_id, news_id, seen_at)
                    VALUES (%s, %s, %s)
                """
                cassandra_session.execute(query, (user_id, action.news_id, datetime.now()))
                processed += 1

                # Инвалидируем кэш
                if redis_client:
                    cache_key = f"action_status:{user_id}:{action.news_id}"
                    redis_client.delete(cache_key)

            else:
                logger.warning(f"Unknown action type: {action.action}")

        logger.info(f"Batch {batch.batch_id}: processed {processed}/{len(batch.actions)} actions for user {user_id}")

        # Запускаем пересчет весов в фоне
        asyncio.create_task(recalculate_user_weights_async(user_id, cassandra_session))

        return BatchResponse(
            status="accepted",
            message="Batch processed successfully",
            batch_id=batch.batch_id,
            received=len(batch.actions),
            processed=processed
        )

    except Exception as e:
        logger.error(f"Error processing batch: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/recalculate-weights")
async def recalculate_weights(
        user_id: int = Depends(get_current_user),
        cassandra_session=Depends(get_cassandra)
):
    """Принудительный пересчет весов для пользователя"""
    try:
        if not cassandra_session:
            raise HTTPException(status_code=503, detail="Cassandra not available")

        recalculator = WeightRecalculator(cassandra_session)
        weights = await recalculator.update_user_weights(user_id)

        return {
            "status": "success",
            "message": "Weights recalculated successfully",
            "user_id": user_id,
            "weights": weights
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error recalculating weights: {e}")
        raise HTTPException(status_code=500, detail=str(e))