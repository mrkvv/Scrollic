from fastapi import APIRouter
from .. import schemas

router = APIRouter(prefix="/api/users", tags=["Users"])


@router.get("/me", response_model=schemas.UserResponse)
def get_me():
    # Заглушка
    return {
        "id": 1,
        "name": "testuser",
        "created_at": "2024-01-01T00:00:00"
    }


@router.put("/me", response_model=schemas.UserResponse)
def update_me(update_data: schemas.UserUpdate):
    # Заглушка
    return {
        "id": 1,
        "name": update_data.name or "testuser",
        "created_at": "2024-01-01T00:00:00"
    }


@router.put("/me/password")
def change_password():
    # Заглушка
    return {"message": "Password changed (stub)"}