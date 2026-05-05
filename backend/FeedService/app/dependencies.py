from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import redis
import logging
from jose import JWTError, jwt
from app.config import config

security = HTTPBearer()
logger = logging.getLogger(__name__)


def get_redis() -> redis.Redis:
    """
    Получение реального Redis клиента с проверкой подключения
    """
    try:
        client = redis.Redis(
            host=config.REDIS_HOST,
            port=config.REDIS_PORT,
            decode_responses=True,
            socket_connect_timeout=3,
            socket_timeout=3
        )
        client.ping()
        logger.info(f"Connected to Redis at {config.REDIS_HOST}:{config.REDIS_PORT}")
        return client
    except Exception as e:
        logger.warning(f"Failed to connect to Redis: {e}")
        return None


async def get_current_user(
        credentials: HTTPAuthorizationCredentials = Depends(security),
        redis_client: redis.Redis = Depends(get_redis)
) -> int:

    token = credentials.credentials

    try:
        payload = jwt.decode(
            token,
            config.SECRET_KEY,
            algorithms=[config.ALGORITHM]
        )

        user_id_str = payload.get("sub")
        if user_id_str is None:
            logger.warning(f"Token missing 'sub' claim")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token: missing user id"
            )

        user_id = int(user_id_str)

        if redis_client:
            session_key = f"session:{token}"
            session_data = redis_client.hgetall(session_key)

            if not session_data:
                logger.warning(f"No session found for token (user_id={user_id})")
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Session expired or not found"
                )

            session_user_id = session_data.get("user_id")
            if session_user_id and int(session_user_id) != user_id:
                logger.error(f"User ID mismatch: token={user_id}, session={session_user_id}")
                raise HTTPException(
                    status_code=status.HTTP_401_UNAUTHORIZED,
                    detail="Token and session mismatch"
                )

            logger.debug(f"Session validated for user {user_id}")
        else:
            logger.warning(f"Redis unavailable, skipping session check for user {user_id}")

        return user_id

    except JWTError as e:
        logger.error(f"JWT validation error: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Invalid token: {str(e)}"
        )
    except ValueError as e:
        logger.error(f"Value error: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid user id in token"
        )
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Unexpected error in get_current_user: {e}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication failed"
        )