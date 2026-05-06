import os
from dotenv import load_dotenv

load_dotenv()


class Config:
    # Cassandra
    CASSANDRA_HOSTS: list = os.getenv("CASSANDRA_HOSTS", "localhost").split(",")
    CASSANDRA_KEYSPACE: str = os.getenv("CASSANDRA_KEYSPACE", "scrollic")

    # Redis
    REDIS_HOST: str = os.getenv("REDIS_HOST", "localhost")
    REDIS_PORT: int = int(os.getenv("REDIS_PORT", "6379"))

    # Service
    SERVICE_PORT: int = int(os.getenv("ACTION_SERVICE_PORT", "8002"))

    # Redis Streams
    STREAM_ACTIONS: str = os.getenv("STREAM_ACTIONS", "user_actions")
    STREAM_CONSUMER_GROUP: str = os.getenv("STREAM_CONSUMER_GROUP", "action_processor")
    STREAM_CONSUMER_NAME: str = os.getenv("STREAM_CONSUMER_NAME", "processor_1")


config = Config()