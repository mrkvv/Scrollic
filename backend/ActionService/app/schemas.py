from pydantic import BaseModel
from uuid import UUID
from typing import List, Optional
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