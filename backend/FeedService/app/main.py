from fastapi import FastAPI
from app.routers import feed, health
from app.config import config

app = FastAPI(
    title="FeedService API",
    description="Сервис формирования персонализированной ленты новостей. Только чтение из Cassandra.",
    version="1.0.0"
)

app.include_router(feed.router)
app.include_router(health.router)


@app.on_event("startup")
async def startup_event():
    """Инициализация при старте (заглушка)"""
    print(f"FeedService starting on port {config.SERVICE_PORT}")
    print(f"Redis: {config.REDIS_HOST}:{config.REDIS_PORT}")
    print(f"Cassandra keyspace: {config.CASSANDRA_KEYSPACE}")


@app.on_event("shutdown")
async def shutdown_event():
    """Очистка при остановке"""
    print("FeedService shutting down")


@app.get("/")
async def root():
    return {"services": "FeedService", "version": "1.0.0"}