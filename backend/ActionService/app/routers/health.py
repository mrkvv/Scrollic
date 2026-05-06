from fastapi import APIRouter
from datetime import datetime
from app.schemas import HealthResponse
from app.dependencies import get_redis
from app.database import get_cassandra

router = APIRouter(tags=["health"])


@router.get("/health", response_model=HealthResponse)
async def health_check():
    """Проверка работоспособности с реальной проверкой Redis"""
    redis_status = "unknown"
    cassandra_status = "unknown"

    # Проверка Redis
    redis_client = get_redis()
    if redis_client:
        try:
            redis_client.ping()
            redis_status = "up"
        except:
            redis_status = "down"
    else:
        redis_status = "not_configured"

    # Проверка Cassandra
    cassandra_session = get_cassandra()
    if cassandra_session:
        try:
            cassandra_session.execute("SELECT now() FROM system.local")
            cassandra_status = "up"
        except:
            cassandra_status = "down"
    else:
        cassandra_status = "not_configured"

    status = "healthy" if cassandra_status == "up" and redis_status in ["up", "not_configured"] else "unhealthy"

    return HealthResponse(
        status=status,
        cassandra=cassandra_status,
        redis=redis_status,
        timestamp=datetime.now()
    )