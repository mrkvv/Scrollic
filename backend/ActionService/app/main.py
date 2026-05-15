import asyncio
import logging
from datetime import datetime, timedelta
from fastapi import FastAPI
from contextlib import asynccontextmanager
from app.routers import actions, health
from app.config import config
from app.database import get_cassandra
from app.services.weight_recalculator import WeightRecalculator
import time
from collections import deque

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

request_times = deque(maxlen=100)


async def periodic_weight_recalculation():
    """Периодически пересчитывать веса для активных пользователей"""
    while True:
        try:
            logger.info("Starting periodic weight recalculation...")
            cassandra_session = get_cassandra()

            if cassandra_session:
                recalculator = WeightRecalculator(cassandra_session)

                hours_ago = datetime.now() - timedelta(hours=24)

                likes_query = """
                    SELECT user_id, liked_at FROM user_likes 
                    ALLOW FILTERING
                """
                active_from_likes = cassandra_session.execute(likes_query)

                seen_query = """
                    SELECT user_id, seen_at FROM user_seen 
                    ALLOW FILTERING
                """
                active_from_seen = cassandra_session.execute(seen_query)

                users = set()
                for row in active_from_likes:
                    if row['liked_at'] >= hours_ago:
                        users.add(row['user_id'])
                for row in active_from_seen:
                    if row['seen_at'] >= hours_ago:
                        users.add(row['user_id'])

                logger.info(f"Found {len(users)} active users for weight update")

                for user_id in users:
                    try:
                        await recalculator.update_user_weights(user_id)
                        logger.info(f"Recalculated weights for user {user_id}")
                    except Exception as e:
                        logger.error(f"Failed to recalculate weights for user {user_id}: {e}")
            else:
                logger.warning("Cassandra not available for periodic weight recalculation")

            await asyncio.sleep(3600)

        except Exception as e:
            logger.error(f"Error in periodic weight recalculation: {e}")
            await asyncio.sleep(60)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info(f"ActionService starting on port {config.SERVICE_PORT}")
    logger.info(f"Redis: {config.REDIS_HOST}:{config.REDIS_PORT}")
    logger.info(f"Cassandra keyspace: {config.CASSANDRA_KEYSPACE}")

    # Запускаем фоновую задачу пересчета весов
    task = asyncio.create_task(periodic_weight_recalculation())

    yield

    # Shutdown
    task.cancel()
    try:
        await task
    except asyncio.CancelledError:
        logger.info("Periodic weight recalculation task cancelled")

    logger.info("ActionService shutting down")


# Создаем приложение
app = FastAPI(
    title="ActionService API",
    description="API для управления действиями пользователя (лайки, просмотры) и пересчета весов",
    version="2.0.0",
    lifespan=lifespan
)


# Middleware для мониторинга (ДОБАВЛЯЕМ ПОСЛЕ создания app)
@app.middleware("http")
async def monitor_requests(request, call_next):
    start_time = time.time()

    # Логируем размер очереди (приблизительно)
    current_queue = len(request_times)
    if current_queue > 50:
        logger.warning(f"High queue size: ~{current_queue} pending requests")

    try:
        response = await call_next(request)
        duration = time.time() - start_time
        request_times.append(duration)

        # Логируем медленные запросы
        if duration > 1.0:
            logger.warning(f"SLOW REQUEST: {request.method} {request.url.path} - {duration:.3f}s")

        return response
    except Exception as e:
        duration = time.time() - start_time
        logger.error(f"REQUEST FAILED: {request.method} {request.url.path} - {duration:.3f}s - {str(e)}")
        raise


# Подключаем роутеры
app.include_router(actions.router)
app.include_router(health.router)


@app.get("/")
async def root():
    return {
        "service": "ActionService",
        "version": "2.0.0",
        "status": "running"
    }