from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from .. import schemas
from ..dependencies import get_current_user_id
from ..database import get_db
from ..models import User
from ..auth import get_password_hash, verify_password

router = APIRouter(prefix="/api/users", tags=["Users"])


@router.get("/me", response_model=schemas.UserResponse)
def get_me(user_id: int = Depends(get_current_user_id), db: Session = Depends(get_db)):
    """Получение профиля текущего пользователя"""

    user = db.query(User).filter(User.id == user_id).first()
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
        user_id: int = Depends(get_current_user_id),
        db: Session = Depends(get_db)
):
    """Обновление профиля"""

    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    if update_data.name is not None:
        existing = db.query(User).filter(User.name == update_data.name).first()
        if existing and existing.id != user_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Username already taken"
            )
        user.name = update_data.name

    db.commit()
    db.refresh(user)

    # TODO: Отправить событие USER_UPDATED в Kafka

    return {
        "id": user.id,
        "name": user.name,
        "created_at": user.created_at
    }


@router.put("/me/password")
def change_password(
        password_data: schemas.PasswordChange,
        user_id: int = Depends(get_current_user_id),
        db: Session = Depends(get_db)
):
    """Смена пароля"""

    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    if not verify_password(password_data.old_password, user.password_hash):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid old password"
        )

    user.password_hash = get_password_hash(password_data.new_password)
    db.commit()

    # TODO: Отправить событие PASSWORD_CHANGED в Kafka

    return {"message": "Password changed successfully"}