from pydantic import BaseModel
from datetime import datetime
from typing import List, Optional
from uuid import UUID


class NewsItem(BaseModel):
    id: UUID
    head: str
    summary: str
    text: str
    url: Optional[str] = None
    url_picture: Optional[str] = None
    popularity: int
    theme_id: int
    created_at: datetime


class FeedResponse(BaseModel):
    feed: List[NewsItem]
    total: int
    limit: int


class HealthResponse(BaseModel):
    status: str
    cassandra: str
    redis: str
    timestamp: datetime


class TokenData(BaseModel):
    user_id: int