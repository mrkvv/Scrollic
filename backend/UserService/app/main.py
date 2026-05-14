from contextlib import asynccontextmanager
from fastapi import FastAPI
from .database import engine, Base
from .redis_client import init_redis, close_redis
from .routers import auth, users


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    await init_redis()

    yield

    await close_redis()
    await engine.dispose()


app = FastAPI(
    title="UserService API",
    description="API для управления пользователями",
    version="3.0.0",
    lifespan=lifespan
)

app.include_router(auth.router)
app.include_router(users.router)


@app.get("/health")
async def health_check():
    from .redis_client import redis_client
    redis_status = "connected" if redis_client else "disconnected"
    return {
        "status": "healthy",
        "service": "user-service",
        "redis": redis_status
    }


@app.get("/hello")
async def hello():
    return {"message": "Hello from UserService!"}