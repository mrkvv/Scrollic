from cassandra.cluster import Cluster
from cassandra.query import dict_factory
from app.config import config
import logging
from datetime import datetime
import time

logger = logging.getLogger(__name__)

class CassandraClient:
    def __init__(self):
        self.cluster = None
        self.session = None
        self.query_count = 0
        self.slow_queries = []

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

            self.session.execute("""
                CREATE TABLE IF NOT EXISTS user_theme_weights (
                    user_id INT,
                    theme_id INT,
                    weight FLOAT,
                    updated_at TIMESTAMP,
                    PRIMARY KEY (user_id, theme_id)
                )
            """)

            self.session.execute("""
                CREATE TABLE IF NOT EXISTS news (
                    id UUID PRIMARY KEY,
                    theme_id INT,
                    title TEXT,
                    content TEXT,
                    created_at TIMESTAMP
                )
            """)

            logger.info(f"Connected to Cassandra, keyspace: {config.CASSANDRA_KEYSPACE}")
            return self.session
        except Exception as e:
            logger.error(f"Failed to connect to Cassandra: {e}")
            return None

    async def execute_async(self, query, params=None):
        """Асинхронное выполнение запроса с метриками"""
        start = time.time()
        self.query_count += 1

        try:
            # ResponseFuture имеет метод result(), который можно использовать
            future = self.session.execute_async(query, params)

            # Ждем результат через метод result() в отдельном потоке
            # или используем add_callback
            loop = asyncio.get_event_loop()
            result = await loop.run_in_executor(None, future.result)

            duration = time.time() - start

            if duration > 0.5:
                logger.warning(f"[CASSANDRA] SLOW QUERY ({duration:.3f}s): {query[:100]}")
                self.slow_queries.append({
                    'query': query[:100],
                    'duration': duration,
                    'time': str(datetime.now())
                })
                if len(self.slow_queries) > 10:
                    self.slow_queries.pop(0)

            return result
        except Exception as e:
            duration = time.time() - start
            logger.error(f"[CASSANDRA] QUERY FAILED ({duration:.3f}s): {str(e)}")
            raise

    def get_session(self):
        if not self.session:
            self.connect()
        return self.session


cassandra_client = CassandraClient()


def get_cassandra():
    """Dependency для получения сессии Cassandra"""
    return cassandra_client.get_session()