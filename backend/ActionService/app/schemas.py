from pydantic import BaseModel
from uuid import UUID
<<<<<<< HEAD
from typing import List, Optional, Dict
=======
from typing import List, Optional
>>>>>>> acb1fe71001f9380ee3fb1074349454278030efc
from datetime import datetime


class Action(BaseModel):
    action_id: str
    news_id: UUID
    action: str  # "like" or "seen"
    timestamp: int


class BatchActions(BaseModel):
    batch_id: str
    client_timestamp: int
    actions: List[Action]


class BatchResponse(BaseModel):
    status: str
    message: str
    batch_id: str
    received: int
    processed: int


class ActionResponse(BaseModel):
    status: str
    message: str
    news_id: UUID


class ActionStatusResponse(BaseModel):
    news_id: UUID
    liked: bool
    seen: bool


class HealthResponse(BaseModel):
    status: str
    cassandra: str
    redis: str
    timestamp: datetime


class LikeRequest(BaseModel):
    news_id: UUID


class SeenRequest(BaseModel):
    news_id: UUID


class UnlikeRequest(BaseModel):
<<<<<<< HEAD
    news_id: UUID


class PreferencesRequest(BaseModel):
    theme_weights: Dict[int, float]  # {theme_id: weight}
=======
    news_id: UUID
>>>>>>> acb1fe71001f9380ee3fb1074349454278030efc
