import logging
import json
from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
from uuid import UUID
from datetime import datetime
import time
from app.schemas import (
    BatchActions, BatchResponse, ActionResponse,
    ActionStatusResponse, LikeRequest, SeenRequest, UnlikeRequest
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


@router.post("/batch", response_model=BatchResponse)
async def batch_actions(
        batch: BatchActions,
        user_id: int = Depends(get_current_user),
        cassandra_session=Depends(get_cassandra),
        redis_client=Depends(get_redis),
        background_tasks: BackgroundTasks = None
):
    """Обработка пачки действий"""
    total_start = time.time()

    logger.info(f"=== BATCH START: {batch.batch_id} for user {user_id} | Actions: {len(batch.actions)} ===")

    try:
        processed = 0

        # Выполняем все вставки (они быстрые, ~1ms каждая)
        for action in batch.actions:
            if action.action == "like":
                query = """
                    INSERT INTO user_likes (user_id, news_id, liked_at)
                    VALUES (%s, %s, %s)
                """
                cassandra_session.execute(query, (user_id, action.news_id, datetime.now()))
                processed += 1

                if redis_client:
                    cache_key = f"action_status:{user_id}:{action.news_id}"
                    redis_client.delete(cache_key)

            elif action.action == "seen":
                query = """
                    INSERT INTO user_seen (user_id, news_id, seen_at)
                    VALUES (%s, %s, %s)
                """
                cassandra_session.execute(query, (user_id, action.news_id, datetime.now()))
                processed += 1

                if redis_client:
                    cache_key = f"action_status:{user_id}:{action.news_id}"
                    redis_client.delete(cache_key)

        total_time = time.time() - total_start
        logger.info(
            f"=== BATCH END: {batch.batch_id} | Processed: {processed}/{len(batch.actions)} | "
            f"Total time: {total_time:.3f}s | Avg per action: {total_time / len(batch.actions):.3f}s ==="
        )

        # Запускаем пересчет весов в фоне
        background_tasks.add_task(recalculate_user_weights_async, user_id, cassandra_session)

        return BatchResponse(
            status="accepted",
            message="Batch processed successfully",
            batch_id=batch.batch_id,
            received=len(batch.actions),
            processed=processed
        )

    except Exception as e:
        logger.error(f"[{batch.batch_id}] BATCH ERROR: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))