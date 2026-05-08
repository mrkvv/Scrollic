import asyncio
import logging
from datetime import datetime, timedelta
from fastapi import FastAPI
from contextlib import asynccontextmanager
from app.routers import actions, health
from app.config import config
from app.database import get_cassandra
from app.services.weight_recalculator import WeightRecalculator

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


async def periodic_weight_recalculation():
    """Периодически пересчитывать веса для активных пользователей"""
    while True:
        try:
            logger.info("Starting periodic weight recalculation...")
            cassandra_session = get_cassandra()

            if cassandra_session:
                recalculator = WeightRecalculator(cassandra_session)

                # Получаем активных пользователей (у которых были действия за последние 24 часа)
                hours_ago = datetime.now() - timedelta(hours=24)

                # Исправленный запрос - без DISTINCT с WHERE
                # Получаем пользователей из лайков
                likes_query = """
                    SELECT user_id, liked_at FROM user_likes 
                    ALLOW FILTERING
                """
                active_from_likes = cassandra_session.execute(likes_query)

                # Получаем пользователей из просмотров
                seen_query = """
                    SELECT user_id, seen_at FROM user_seen 
                    ALLOW FILTERING
                """
                active_from_seen = cassandra_session.execute(seen_query)

                # Объединяем уникальных пользователей с активностью за последние 24 часа
                users = set()
                for row in active_from_likes:
                    if row['liked_at'] >= hours_ago:
                        users.add(row['user_id'])
                for row in active_from_seen:
                    if row['seen_at'] >= hours_ago:
                        users.add(row['user_id'])

                logger.info(f"Found {len(users)} active users for weight update")

                # Пересчитываем веса для каждого пользователя
                for user_id in users:
                    try:
                        await recalculator.update_user_weights(user_id)
                        logger.info(f"Recalculated weights for user {user_id}")
                    except Exception as e:
                        logger.error(f"Failed to recalculate weights for user {user_id}: {e}")
            else:
                logger.warning("Cassandra not available for periodic weight recalculation")

            # Ждем 1 час до следующего пересчета
            await asyncio.sleep(3600)  # 1 час

        except Exception as e:
            logger.error(f"Error in periodic weight recalculation: {e}")
            await asyncio.sleep(60)  # При ошибке ждем 1 минуту

            

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