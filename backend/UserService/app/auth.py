from datetime import datetime
from jose import JWTError, jwt
from passlib.context import CryptContext
from .config import config

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Проверка пароля"""
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """Хэширование пароля"""
    return pwd_context.hash(password)


def validate_token(token: str) -> bool:
    """
    Валидация токена от APIGateway.
    Возвращает True если токен валидный, иначе False.
    """
    try:
        payload = jwt.decode(
            token,
            config.JWT_SECRET_KEY,
            algorithms=[config.JWT_ALGORITHM]
        )
        # Проверка срока действия
        exp = payload.get("exp")
        if exp and datetime.utcnow().timestamp() > exp:
            return False
        return True
    except JWTError:
        return False


def extract_user_id_from_token(token: str) -> int | None:
    """Извлекает user_id из токена APIGateway"""
    try:
        payload = jwt.decode(
            token,
            config.JWT_SECRET_KEY,
            algorithms=[config.JWT_ALGORITHM]
        )
        # Проверка срока действия
        exp = payload.get("exp")
        if exp and datetime.utcnow().timestamp() > exp:
            return None

        # APIGateway должен класть user_id в поле "sub"
        user_id = payload.get("sub")
        return user_id if user_id else None
    except JWTError:
        return None