import os
from dotenv import load_dotenv
from pathlib import Path

# Загружаем корневой .env (общий для всех сервисов)
root_env = Path(__file__).parent.parent.parent.parent / '.env'
if root_env.exists():
    load_dotenv(root_env, override=False)


class Config:
    # PostgreSQL
    POSTGRES_HOST: str = os.getenv("POSTGRES_HOST", "localhost")
    POSTGRES_PORT: str = os.getenv("POSTGRES_PORT", "5432")
    POSTGRES_DB: str = os.getenv("POSTGRES_DB", "scrollic_db")
    POSTGRES_USER: str = os.getenv("POSTGRES_USER", "postgres")
    POSTGRES_PASSWORD: str = os.getenv("POSTGRES_PASSWORD", "postgres")

    @property
    def DATABASE_URL(self) -> str:
        return f"postgresql://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"

    # JWT (для валидации токенов от APIGateway)
    JWT_SECRET_KEY: str = os.getenv("SECRET_KEY", "shared-secret-with-api-gateway")
    JWT_ALGORITHM: str = "HS256"

    # Redis
    REDIS_HOST: str = os.getenv("REDIS_HOST", "redis")
    REDIS_PORT: int = int(os.getenv("REDIS_PORT", "6379"))
    REDIS_DB: int = int(os.getenv("REDIS_DB", "0"))
    REDIS_PASSWORD: str = os.getenv("REDIS_PASSWORD", None)

    # Service
    SERVICE_PORT: int = int(os.getenv("USER_SERVICE_PORT", "8001"))
    SERVICE_HOST: str = "0.0.0.0"


config = Config()