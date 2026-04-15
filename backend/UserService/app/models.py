from sqlalchemy import Column, Integer, String, DateTime, Sequence
from sqlalchemy.sql import func
from .database import Base


class User(Base):
    __tablename__ = "users"
    __table_args__ = {"schema": "public"}  # Явно указываем схему

    id = Column(Integer, Sequence('users_id_seq'), primary_key=True, index=True)
    name = Column(String(100), unique=True, nullable=False, index=True)
    password = Column(String(255), nullable=False)
    created_at = Column(DateTime, server_default=func.now())