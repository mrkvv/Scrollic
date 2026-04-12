from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from .. import schemas
from ..dependencies import get_current_user_id
from ..database import get_db
from ..models import User
from ..auth import verify_password, get_password_hash
from ..redis_client import get_redis
from datetime import datetime
import redis
import json
import uuid

router = APIRouter(prefix="/api/auth", tags=["Auth"])


@router.post("/register", response_model=schemas.TokenResponse, status_code=status.HTTP_201_CREATED)
def register(user_data: schemas.UserRegister, db: Session = Depends(get_db)):
    """Регистрация пользователя"""

    existing_user = db.query(User).filter(User.name == user_data.name).first()
    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Username already registered"
        )

    hashed_password = get_password_hash(user_data.password)
    new_user = User(
        name=user_data.name,
        password_hash=hashed_password,
        created_at=datetime.utcnow()
    )

    db.add(new_user)
    db.commit()
    db.refresh(new_user)

    # Токен приходит от API Gateway, здесь мы его не генерируем
    return {
        "access_token": "token_from_api_gateway_will_be_here",
        "token_type": "bearer",
        "expires_in": 3600,
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
    """Логин пользователя - создаем сессию в Redis"""

    user = db.query(User).filter(User.name == user_data.name).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password"
        )

    if not verify_password(user_data.password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password"
        )

    # Создаем сессию в Redis (если Redis доступен)
    if redis_client:
        session_id = str(uuid.uuid4())
        session_data = {
            "user_id": user.id,
            "user_name": user.name,
            "login_time": datetime.utcnow().isoformat()
        }
        # Храним сессию 3600 секунд (как время жизни токена)
        redis_client.setex(
            f"session:{session_id}",
            3600,
            json.dumps(session_data)
        )
        print(f"✅ Session created for user {user.name}: {session_id}")
    else:
        print("⚠️ Redis not available, session not created")

    # Токен приходит от API Gateway, здесь мы его не генерируем
    return {
        "access_token": "token_from_api_gateway_will_be_here",
        "token_type": "bearer",
        "expires_in": 3600,
        "user": {
            "id": user.id,
            "name": user.name,
            "created_at": user.created_at
        }
    }


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout(
        user_id: int = Depends(get_current_user_id),
        db: Session = Depends(get_db),
        redis_client: redis.Redis = Depends(get_redis),
        credentials: HTTPAuthorizationCredentials = Depends(HTTPBearer())
):
    """Выход из системы - удаляем сессию и добавляем токен в blacklist"""

    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    token = credentials.credentials

    if redis_client:
        # 1. Добавляем токен в blacklist
        token_key = f"blacklist:{token}"
        redis_client.setex(token_key, 3600, "revoked")

        # 2. Удаляем все сессии пользователя (опционально)
        # Ищем и удаляем сессии этого пользователя
        session_keys = redis_client.keys("session:*")
        for session_key in session_keys:
            session_data = redis_client.get(session_key)
            if session_data:
                data = json.loads(session_data)
                if data.get("user_id") == user_id:
                    redis_client.delete(session_key)
                    print(f"✅ Session deleted for user {user.name}: {session_key}")

        print(f"✅ Token blacklisted and sessions cleared for user {user.name}")
    else:
        print(f"⚠️ Redis not available, token not blacklisted and sessions not cleared")

    return None