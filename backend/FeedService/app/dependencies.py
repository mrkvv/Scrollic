from fastapi import Header, HTTPException, status
import logging

logger = logging.getLogger(__name__)


async def get_current_user(
        x_user_id: int = Header(..., alias="X-User-Id")
) -> int:
    """Получение user_id из заголовка X-User-Id, который добавляет Gateway"""
    if not x_user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing X-User-Id header"
        )

    logger.debug(f"User authenticated via Gateway: user_id={x_user_id}")
    return x_user_id