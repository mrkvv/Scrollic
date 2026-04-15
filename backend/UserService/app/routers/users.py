from fastapi import APIRouter, Depends, HTTPException, status, Header
from sqlalchemy.orm import Session
from .. import schemas
from ..database import get_db
from ..models import User
from ..auth import verify_password, get_password_hash

router = APIRouter(prefix="/api/users", tags=["Users"])


@router.get("/me", response_model=schemas.UserResponse)
def get_me(
        x_user_id: int = Header(..., alias="X-User-Id"),
        db: Session = Depends(get_db)
):
    """Получение профиля текущего пользователя"""

    user = db.query(User).filter(User.id == x_user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    return {
        "id": user.id,
        "name": user.name,
        "created_at": user.created_at
    }


@router.put("/me", response_model=schemas.UserResponse)
def update_me(
        update_data: schemas.UserUpdate,
        x_user_id: int = Header(..., alias="X-User-Id"),
        db: Session = Depends(get_db)
):
    """Обновление профиля"""

    user = db.query(User).filter(User.id == x_user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    if update_data.name is not None:
        # Проверяем, не занято ли имя другим пользователем
        existing = db.query(User).filter(User.name == update_data.name).first()
        if existing and existing.id != x_user_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Username already taken"
            )
        user.name = update_data.name

    db.commit()
    db.refresh(user)

    return {
        "id": user.id,
        "name": user.name,
        "created_at": user.created_at
    }


@router.put("/me/password", status_code=status.HTTP_204_NO_CONTENT)
def change_password(
        password_data: schemas.PasswordChange,
        x_user_id: int = Header(..., alias="X-User-Id"),
        db: Session = Depends(get_db)
):
    """Смена пароля"""

    user = db.query(User).filter(User.id == x_user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    if not verify_password(password_data.old_password, user.password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid old password"
        )

    user.password = get_password_hash(password_data.new_password)
    db.commit()

    return None