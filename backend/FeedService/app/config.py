import os
from dotenv import load_dotenv

load_dotenv()


class Config:
    # Cassandra
    CASSANDRA_HOSTS: list = os.getenv("CASSANDRA_HOSTS", "localhost").split(",")
    CASSANDRA_KEYSPACE: str = os.getenv("CASSANDRA_KEYSPACE", "scrollic")

    # Service
    SERVICE_PORT: int = int(os.getenv("FEED_SERVICE_PORT", "8003"))

    # Feed limits
    MAX_LIMIT: int = 100
    DEFAULT_LIMIT: int = 50


config = Config()