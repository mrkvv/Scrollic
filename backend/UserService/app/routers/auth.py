from fastapi import APIRouter, HTTPException, status
from .. import schemas

router = APIRouter(prefix="/api/auth", tags=["Auth"])


@router.post("/register", response_model=schemas.TokenResponse, status_code=status.HTTP_201_CREATED)
def register(user_data: schemas.UserRegister):
    # Заглушка
    return {
        "access_token": "fake_token_123",
        "token_type": "bearer",
        "expires_in": 3600,
        "user": {
            "id": 1,
            "name": user_data.name,
            "created_at": "2024-01-01T00:00:00"
        }
    }


@router.post("/login", response_model=schemas.TokenResponse)
def login(user_data: schemas.UserLogin):
    # Заглушка
    return {
        "access_token": "fake_token_123",
        "token_type": "bearer",
        "expires_in": 3600,
        "user": {
            "id": 1,
            "name": user_data.name,
            "created_at": "2024-01-01T00:00:00"
        }
    }


@router.post("/logout", status_code=status.HTTP_204_NO_CONTENT)
def logout():
    return None