from fastapi import APIRouter, HTTPException, status, Depends
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from datetime import datetime, timedelta
import redis.asyncio as redis

from .. import schemas
from ..database import get_db
from ..models import User
from ..auth import verify_password, get_password_hash, create_access_token
from ..redis_client import get_redis
from ..config import config

router = APIRouter(prefix="/api/auth", tags=["Auth"])
security = HTTPBearer()


@router.post("/register", response_model=schemas.TokenResponse, status_code=status.HTTP_201_CREATED)
async def register(
        user_data: schemas.UserRegister,
        db: AsyncSession = Depends(get_db),
        redis_client: redis.Redis = Depends(get_redis)
):
    result = await db.execute(select(User).where(User.name == user_data.name))
    existing_user = result.scalar_one_or_none()

    if existing_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Username already registered"
        )

    hashed_password = get_password_hash(user_data.password)

    new_user = User(
        name=user_data.name,
        password=hashed_password
    )

    db.add(new_user)
    await db.flush()
    await db.refresh(new_user)

    access_token = create_access_token(data={"sub": str(new_user.id)})
    expires_at = datetime.utcnow() + timedelta(minutes=config.ACCESS_TOKEN_EXPIRE_MINUTES)

    if redis_client:
        await redis_client.hset(
            f"session:{access_token}",
            mapping={
                "user_id": new_user.id,
                "user_name": new_user.name,
                "expires_at": expires_at.isoformat()
            }
        )
        await redis_client.expire(f"session:{access_token}", config.ACCESS_TOKEN_EXPIRE_MINUTES * 60)

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
async def login(
        user_data: schemas.UserLogin,
        db: AsyncSession = Depends(get_db),
        redis_client: redis.Redis = Depends(get_redis)
):
    result = await db.execute(select(User).where(User.name == user_data.name))
    user = result.scalar_one_or_none()

    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password"
        )

    password_ok = verify_password(user_data.password, user.password)

    if not password_ok:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid username or password"
        )

    access_token = create_access_token(data={"sub": str(user.id)})
    expires_at = datetime.utcnow() + timedelta(minutes=config.ACCESS_TOKEN_EXPIRE_MINUTES)

    if redis_client:
        await redis_client.hset(
            f"session:{access_token}",
            mapping={
                "user_id": user.id,
                "user_name": user.name,
                "expires_at": expires_at.isoformat()
            }
        )
        await redis_client.expire(f"session:{access_token}", config.ACCESS_TOKEN_EXPIRE_MINUTES * 60)

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
async def logout(
        credentials: HTTPAuthorizationCredentials = Depends(security),
        redis_client: redis.Redis = Depends(get_redis)
):
    token = credentials.credentials

    if redis_client:
        await redis_client.delete(f"session:{token}")

    return None