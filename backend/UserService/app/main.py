from fastapi import FastAPI
from .database import engine, Base
from .config import config
from .redis_client import redis_client

# Импорт моделей
from .models import User

# ОТЛАДКА: проверим, что зарегистрировано в метаданных
print("=== DEBUG: Tables in metadata before create_all ===")
print(Base.metadata.tables.keys())

# Создание таблиц
Base.metadata.create_all(bind=engine)

print("=== DEBUG: Tables created ===")

from .routers import auth, users

app = FastAPI(
    title="UserService API",
    description="API для управления пользователями. Аутентификация через API Gateway (заголовки X-User-Id, X-Username)",
    version="2.0.0"
)

# Подключение роутеров
app.include_router(auth.router)
app.include_router(users.router)


@app.get("/health")
def health_check():
    redis_status = "connected" if redis_client and redis_client.ping() else "disconnected"
    return {
        "status": "healthy",
        "service": "user-service",
        "redis": redis_status
    }


@app.get("/hello")
def hello():
    return {"message": "Hello from UserService!"}