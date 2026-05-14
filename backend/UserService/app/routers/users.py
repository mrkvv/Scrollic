from fastapi import APIRouter, Depends, HTTPException, status, Header
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from .. import schemas
from ..database import get_db
from ..models import User
from ..auth import verify_password, get_password_hash

router = APIRouter(prefix="/api/users", tags=["Users"])


@router.get("/me", response_model=schemas.UserResponse)
async def get_me(
        x_user_id: int = Header(..., alias="X-User-Id"),
        db: AsyncSession = Depends(get_db)
):
    result = await db.execute(select(User).where(User.id == x_user_id))
    user = result.scalar_one_or_none()

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
async def update_me(
        update_data: schemas.UserUpdate,
        x_user_id: int = Header(..., alias="X-User-Id"),
        db: AsyncSession = Depends(get_db)
):
    result = await db.execute(select(User).where(User.id == x_user_id))
    user = result.scalar_one_or_none()

    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    if update_data.name is not None:
        result = await db.execute(select(User).where(User.name == update_data.name))
        existing = result.scalar_one_or_none()

        if existing and existing.id != x_user_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Username already taken"
            )
        user.name = update_data.name

    await db.flush()
    await db.refresh(user)

    return {
        "id": user.id,
        "name": user.name,
        "created_at": user.created_at
    }


@router.put("/me/password", status_code=status.HTTP_204_NO_CONTENT)
async def change_password(
        password_data: schemas.PasswordChange,
        x_user_id: int = Header(..., alias="X-User-Id"),
        db: AsyncSession = Depends(get_db)
):
    result = await db.execute(select(User).where(User.id == x_user_id))
    user = result.scalar_one_or_none()

    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    password_ok = verify_password(password_data.old_password, user.password)

    if not password_ok:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid old password"
        )

    new_hash = get_password_hash(password_data.new_password)
    user.password = new_hash
    await db.flush()

    return None