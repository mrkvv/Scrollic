from cassandra.cluster import Cluster
from cassandra.query import dict_factory
from app.config import config
import logging

logger = logging.getLogger(__name__)


class CassandraClient:
    def __init__(self):
        self.cluster = None
        self.session = None

    def connect(self):
        """Подключение к Cassandra"""
        try:
            logger.info(f"Connecting to Cassandra at {config.CASSANDRA_HOSTS}")
            self.cluster = Cluster(config.CASSANDRA_HOSTS)
            self.session = self.cluster.connect()
            self.session.row_factory = dict_factory

            # Создаем keyspace если нет
            self.session.execute(f"""
                CREATE KEYSPACE IF NOT EXISTS {config.CASSANDRA_KEYSPACE}
                WITH replication = {{'class': 'SimpleStrategy', 'replication_factor': 1}}
            """)

            self.session.set_keyspace(config.CASSANDRA_KEYSPACE)

            # Создаем таблицы если нет
            self.session.execute("""
                CREATE TABLE IF NOT EXISTS user_likes (
                    user_id INT,
                    news_id UUID,
                    liked_at TIMESTAMP,
                    PRIMARY KEY (user_id, news_id)
                )
            """)

            self.session.execute("""
                CREATE TABLE IF NOT EXISTS user_seen (
                    user_id INT,
                    news_id UUID,
                    seen_at TIMESTAMP,
                    PRIMARY KEY (user_id, news_id)
                ) WITH default_time_to_live = 604800
            """)

            logger.info(f"Connected to Cassandra, keyspace: {config.CASSANDRA_KEYSPACE}")
            return self.session
        except Exception as e:
            logger.error(f"Failed to connect to Cassandra: {e}")
            return None

    def get_session(self):
        if not self.session:
            self.connect()
        return self.session


cassandra_client = CassandraClient()


def get_cassandra():
    """Dependency для получения сессии Cassandra"""
    return cassandra_client.get_session()