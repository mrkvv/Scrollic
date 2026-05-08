import logging
from datetime import datetime, timedelta
from collections import defaultdict
from typing import Dict
from cassandra.cluster import Session

logger = logging.getLogger(__name__)


class WeightRecalculator:
    """Пересчет весов тем пользователя на основе действий"""

    def __init__(self, cassandra_session: Session):
        self.session = cassandra_session

    async def get_user_actions_stats(self, user_id: int, days: int = 7) -> Dict[int, Dict]:
        """
        Получить статистику действий пользователя за N дней
        Возвращает: {theme_id: {"likes": int, "views": int}}
        """
        days_ago = datetime.now() - timedelta(days=days)

        # Получаем все лайки пользователя с ALLOW FILTERING
        likes_query = """
            SELECT news_id FROM user_likes 
            WHERE user_id = %s AND liked_at >= %s
            ALLOW FILTERING
        """
        likes = self.session.execute(likes_query, (user_id, days_ago))
        liked_news_ids = [row['news_id'] for row in likes]

        # Получаем все просмотры пользователя с ALLOW FILTERING
        seen_query = """
            SELECT news_id FROM user_seen 
            WHERE user_id = %s AND seen_at >= %s
            ALLOW FILTERING
        """
        seen = self.session.execute(seen_query, (user_id, days_ago))
        seen_news_ids = [row['news_id'] for row in seen]

        # Получаем темы для всех новостей
        all_news_ids = set(liked_news_ids + seen_news_ids)
        news_themes = {}

        for news_id in all_news_ids:
            theme = await self._get_news_theme(news_id)
            if theme:
                news_themes[news_id] = theme

        # Собираем статистику по темам
        theme_stats = defaultdict(lambda: {"likes": 0, "views": 0})

        for news_id in liked_news_ids:
            theme = news_themes.get(news_id)
            if theme:
                theme_stats[theme]["likes"] += 1

        for news_id in seen_news_ids:
            theme = news_themes.get(news_id)
            if theme:
                theme_stats[theme]["views"] += 1

        logger.info(f"User {user_id} has actions in {len(theme_stats)} themes")
        return dict(theme_stats)

    async def _get_news_theme(self, news_id) -> int:
        """Получить тему новости по ID"""
        query = "SELECT theme_id FROM news WHERE id = %s"
        result = self.session.execute(query, (news_id,))
        row = result.one()
        return row['theme_id'] if row else None

    async def calculate_theme_weights(self, user_id: int) -> Dict[int, float]:
        """
        Рассчитать веса тем на основе действий пользователя
        Вес = (лайки_темы * 2 + просмотры_темы) / общее_количество_действий * 100
        """
        stats = await self.get_user_actions_stats(user_id)

        if not stats:
            logger.info(f"No actions for user {user_id}, using default weights")
            return {1: 20.0, 2: 20.0, 3: 20.0, 4: 20.0, 5: 20.0}

        # Рассчитываем общий вес
        total_weight = sum(data["likes"] * 2 + data["views"] for data in stats.values())

        if total_weight == 0:
            return {1: 20.0, 2: 20.0, 3: 20.0, 4: 20.0, 5: 20.0}

        # Вычисляем процент для каждой темы
        weights = {}
        for theme_id, data in stats.items():
            theme_weight = (data["likes"] * 2 + data["views"]) / total_weight * 100
            weights[theme_id] = round(theme_weight, 2)

        logger.info(f"Calculated weights for user {user_id}: {weights}")
        return weights

    async def update_user_weights(self, user_id: int):
        """Обновить веса пользователя в таблице user_theme_weights"""
        try:
            # Рассчитываем новые веса
            weights = await self.calculate_theme_weights(user_id)

            # Удаляем старые веса
            delete_query = "DELETE FROM user_theme_weights WHERE user_id = %s"
            self.session.execute(delete_query, (user_id,))

            # Вставляем новые веса
            insert_query = """
                INSERT INTO user_theme_weights (user_id, theme_id, weight, updated_at)
                VALUES (%s, %s, %s, %s)
            """

            for theme_id, weight in weights.items():
                self.session.execute(insert_query, (user_id, theme_id, weight, datetime.now()))

            logger.info(f"Updated weights for user {user_id}: {len(weights)} themes -> {weights}")

            # Инвалидируем кэш FeedService (через Redis)
            await self._invalidate_feed_cache(user_id)

            return weights

        except Exception as e:
            logger.error(f"Error updating weights for user {user_id}: {e}")
            raise

    async def _invalidate_feed_cache(self, user_id: int):
        """Инвалидировать кэш ленты пользователя в Redis"""
        try:
            from app.dependencies import get_redis
            redis_client = get_redis()

            if redis_client:
                # Удаляем все ключи кэша для этого пользователя
                pattern = f"feed:{user_id}:*"
                keys = redis_client.keys(pattern)
                if keys:
                    redis_client.delete(*keys)
                    logger.info(f"Invalidated {len(keys)} feed cache keys for user {user_id}")
                else:
                    logger.debug(f"No feed cache keys found for user {user_id}")
        except Exception as e:
            logger.warning(f"Failed to invalidate feed cache: {e}")