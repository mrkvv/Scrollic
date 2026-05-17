from fastapi import APIRouter
from datetime import datetime
from app.schemas import HealthResponse
from app.database import get_cassandra

router = APIRouter(tags=["health"])


@router.get("/health")
async def health_check():
    """Проверка работоспособности - только Cassandra"""
    cassandra_status = "unknown"

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

    status = "healthy" if cassandra_status == "up" else "unhealthy"

    return HealthResponse(
        status=status,
        cassandra=cassandra_status,
        redis="not_used",
        timestamp=datetime.now()
    )
