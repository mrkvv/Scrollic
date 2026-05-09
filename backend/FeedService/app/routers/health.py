from fastapi import APIRouter, Depends
from datetime import datetime
from app.schemas import HealthResponse
from app.dependencies import get_redis

router = APIRouter(tags=["health"])


@router.get("/health")
async def health_check(redis_client=Depends(get_redis)):
    """Проверка работоспособности с реальной проверкой Redis"""
    redis_status = "unknown"

    if redis_client:
        try:
            redis_client.ping()
            redis_status = "up"
        except:
            redis_status = "down"
    else:
        redis_status = "not_configured"

    cassandra_status = "not_configured"

    status = "healthy" if redis_status in ["up", "not_configured"] else "unhealthy"

    return HealthResponse(
        status=status,
        cassandra=cassandra_status,
        redis=redis_status,
        timestamp=datetime.now()
    )