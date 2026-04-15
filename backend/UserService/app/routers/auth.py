from fastapi import APIRouter, HTTPException, status, Depends, Header  # <-- ДОБАВИТЬ Header
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
import uuid

router = APIRouter(prefix="/api/auth", tags=["Auth"])


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

    # Генерация токена
    access_token = create_access_token(data={"sub": str(new_user.id)})

    # Создаем сессию в Redis (если Redis доступен)
    if redis_client:
        session_id = str(uuid.uuid4())
        session_data = {
            "user_id": new_user.id,
            "user_name": new_user.name,
            "login_time": datetime.utcnow().isoformat()
        }
        redis_client.setex(
            f"session:{session_id}",
            config.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
            json.dumps(session_data)
        )

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

    # Генерация токена
    access_token = create_access_token(data={"sub": str(user.id)})

    # Создаем сессию в Redis
    if redis_client:
        session_id = str(uuid.uuid4())
        session_data = {
            "user_id": user.id,
            "user_name": user.name,
            "login_time": datetime.utcnow().isoformat()
        }
        redis_client.setex(
            f"session:{session_id}",
            config.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
            json.dumps(session_data)
        )

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
        x_user_id: int = Header(..., alias="X-User-Id"),
        redis_client: redis.Redis = Depends(get_redis)
):
    """Выход из системы - удаляем все сессии пользователя из Redis"""

    if redis_client:
        # Ищем и удаляем все сессии этого пользователя
        session_keys = redis_client.keys("session:*")
        deleted_count = 0
        for session_key in session_keys:
            session_data = redis_client.get(session_key)
            if session_data:
                data = json.loads(session_data)
                if data.get("user_id") == x_user_id:
                    redis_client.delete(session_key)
                    deleted_count += 1

        if deleted_count > 0:
            print(f"Deleted {deleted_count} sessions for user {x_user_id}")

    return None