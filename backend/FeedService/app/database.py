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
            self.session = self.cluster.connect(config.CASSANDRA_KEYSPACE)
            self.session.row_factory = dict_factory
            logger.info(f"Connected to Cassandra, keyspace: {config.CASSANDRA_KEYSPACE}")

            self.session.execute("SELECT now() FROM system.local")
            logger.info("Cassandra is ready")

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
    session = cassandra_client.get_session()
    if not session:
        logger.warning("Cassandra session is None")
    return session