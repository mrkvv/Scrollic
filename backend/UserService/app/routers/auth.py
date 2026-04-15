from fastapi import APIRouter, HTTPException, status, Depends, Header
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from datetime import datetime, timedelta
from .. import schemas
from ..database import get_db
from ..models import User
from ..auth import verify_password, get_password_hash, create_access_token
from ..redis_client import get_redis
from ..config import config
import redis
import json

router = APIRouter(prefix="/api/auth", tags=["Auth"])
security = HTTPBearer()


@router.post("/register", response_model=schemas.TokenResponse, status_code=status.HTTP_201_CREATED)
def register(
        user_data: schemas.UserRegister,
        db: Session = Depends(get_db),
        redis_client: redis.Redis = Depends(get_redis)
):
    """Регистрация пользователя"""

    # Проверка существующего пользователя
    existing_user = db.query(User).filter(User.name == user_data.name).first()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Username already registered"
        )

    # Создание нового пользователя
    hashed_password = get_password_hash(user_data.password)
    new_user = User(
        name=user_data.name,
        password=hashed_password,
        created_at=datetime.utcnow()
    )

    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    # Генерация токена (Bearer Token)
    access_token = create_access_token(data={"sub": str(new_user.id)})

    # Сохраняем сессию в Redis: ключ = сам токен
    if redis_client:
        redis_client.hset(
            f"session:{access_token}",
            mapping={
                "user_id": new_user.id,
                "user_name": new_user.name,
                "expires_at": (datetime.utcnow() + timedelta(minutes=config.ACCESS_TOKEN_EXPIRE_MINUTES)).isoformat()
            }
        )
        redis_client.expire(f"session:{access_token}", config.ACCESS_TOKEN_EXPIRE_MINUTES * 60)

    return {
        "access_token": access_token,
        "token_type": "bearer",
        "expires_in": config.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        "user": {
            "id": new_user.id,
            "name": new_user.name,
            "created_at": new_user.created_at
        }
    }


@router.post("/login", response_model=schemas.TokenResponse)
def login(
        user_data: schemas.UserLogin,
        db: Session = Depends(get_db),
        redis_client: redis.Redis = Depends(get_redis)
):
    """Логин пользователя"""

    user = db.query(User).filter(User.name == user_data.name).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password"
        )

    if not verify_password(user_data.password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password"
        )

    # Генерация токена (Bearer Token)
    access_token = create_access_token(data={"sub": str(user.id)})

    # Сохраняем сессию в Redis: ключ = сам токен
    if redis_client:
        redis_client.hset(
            f"session:{access_token}",
            mapping={
                "user_id": user.id,
                "user_name": user.name,
                "expires_at": (datetime.utcnow() + timedelta(minutes=config.ACCESS_TOKEN_EXPIRE_MINUTES)).isoformat()
            }
        )
        redis_client.expire(f"session:{access_token}", config.ACCESS_TOKEN_EXPIRE_MINUTES * 60)

    return {
        "access_token": access_token,
        "token_type": "bearer",
        "expires_in": config.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
        "user": {
            "id": user.id,
            "name": user.name,
            "created_at": user.created_at
        }
    }


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout(
        credentials: HTTPAuthorizationCredentials = Depends(security),
        redis_client: redis.Redis = Depends(get_redis)
):
    """Выход из системы - удаляем запись из Redis по токену"""

    token = credentials.credentials

    if redis_client:
        # Удаляем сессию по ключу = сам токен
        redis_client.delete(f"session:{token}")
        print(f"Session deleted for token: {token[:20]}...")

    return None