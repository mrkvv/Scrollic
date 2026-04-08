from fastapi import FastAPI
from .routers import auth, users
from .database import engine, Base
from .config import config

# Создание таблиц в БД
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="UserService API",
    description="API для управления пользователями с Bearer Token аутентификацией и Redis",
    version="1.0.0"
)

# Подключение роутеров
app.include_router(auth.router)
app.include_router(users.router)


@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "user-service"}


@app.get("/hello")
def hello():
    return {"message": "Hello from UserService!"}