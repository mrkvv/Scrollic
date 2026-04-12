from fastapi import FastAPI
from .routers import auth, users
from .database import engine, Base
from .config import config
from .redis_client import redis_client

# Создание таблиц в БД
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="UserService API",
    description="API для управления пользователями. Токены приходят от APIGateway.",
    version="1.0.0"
)

# Подключение роутеров
app.include_router(auth.router)
app.include_router(users.router)


@app.get("/health")
def health_check():
    # Проверяем соединение с Redis
    try:
        redis_client.ping()
        redis_status = "connected"
    except Exception as e:
        redis_status = f"error: {str(e)}"

    return {
        "status": "healthy",
        "service": "user-service",
        "redis": redis_status
    }


@app.get("/hello")
def hello():
    return {"message": "Hello from UserService!"}


@app.on_event("shutdown")
def shutdown_event():
    """Закрываем соединение с Redis при выключении"""
    redis_client.close()