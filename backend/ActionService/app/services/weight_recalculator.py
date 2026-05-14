import logging
from datetime import datetime, timedelta
from collections import defaultdict
from typing import Dict, Set
import time
import asyncio

logger = logging.getLogger(__name__)


class WeightRecalculator:
    def __init__(self, cassandra_session):
        self.session = cassandra_session

    async def execute_query(self, query, params=None):
        """Вспомогательный метод для асинхронного выполнения"""
        future = self.session.execute_async(query, params)
        return await asyncio.wrap_future(future)

    async def get_user_actions_stats(self, user_id: int, days: int = 7) -> Dict[int, Dict]:
        days_ago = datetime.now() - timedelta(days=days)

        likes_query = """
            SELECT news_id FROM user_likes 
            WHERE user_id = %s AND liked_at >= %s
            ALLOW FILTERING
        """
        likes_future = self.session.execute_async(likes_query, (user_id, days_ago))
        likes = await asyncio.wrap_future(likes_future)
        liked_news_ids = [row['news_id'] for row in likes]

        seen_query = """
            SELECT news_id FROM user_seen 
            WHERE user_id = %s AND seen_at >= %s
            ALLOW FILTERING
        """
        seen_future = self.session.execute_async(seen_query, (user_id, days_ago))
        seen = await asyncio.wrap_future(seen_future)
        seen_news_ids = [row['news_id'] for row in seen]

        all_news_ids = set(liked_news_ids + seen_news_ids)
        news_themes = await self._get_news_themes_batch(all_news_ids)

        theme_stats = defaultdict(lambda: {"likes": 0, "views": 0})

        for news_id in liked_news_ids:
            theme = news_themes.get(news_id)
            if theme:
                theme_stats[theme]["likes"] += 1

        for news_id in seen_news_ids:
            theme = news_themes.get(news_id)
            if theme:
                theme_stats[theme]["views"] += 1

        return dict(theme_stats)

    async def _get_news_themes_batch(self, news_ids: Set) -> Dict:
        if not news_ids:
            return {}

        placeholders = ','.join(['%s'] * len(news_ids))
        query = f"SELECT id, theme_id FROM news WHERE id IN ({placeholders})"
        future = self.session.execute_async(query, list(news_ids))
        rows = await asyncio.wrap_future(future)

        return {row['id']: row['theme_id'] for row in rows}

    async def calculate_theme_weights(self, user_id: int) -> Dict[int, float]:
        stats = await self.get_user_actions_stats(user_id)

        if not stats:
            return {1: 20.0, 2: 20.0, 3: 20.0, 4: 20.0, 5: 20.0}

        total_weight = sum(data["likes"] * 2 + data["views"] for data in stats.values())

        if total_weight == 0:
            return {1: 20.0, 2: 20.0, 3: 20.0, 4: 20.0, 5: 20.0}

        weights = {}
        for theme_id, data in stats.items():
            theme_weight = (data["likes"] * 2 + data["views"]) / total_weight * 100
            weights[theme_id] = round(theme_weight, 2)

        return weights

    async def update_user_weights(self, user_id: int):
        start_time = time.time()

        logger.info(f"[WEIGHT] Starting recalculation for user {user_id}")

        try:
            weights = await self.calculate_theme_weights(user_id)

            delete_query = "DELETE FROM user_theme_weights WHERE user_id = %s"
            delete_future = self.session.execute_async(delete_query, (user_id,))
            await asyncio.wrap_future(delete_future)

            insert_query = """
                INSERT INTO user_theme_weights (user_id, theme_id, weight, updated_at)
                VALUES (%s, %s, %s, %s)
            """
            insert_tasks = []
            for theme_id, weight in weights.items():
                future = self.session.execute_async(insert_query, (user_id, theme_id, weight, datetime.now()))
                insert_tasks.append(asyncio.wrap_future(future))

            if insert_tasks:
                await asyncio.gather(*insert_tasks)

            total_time = time.time() - start_time
            logger.info(f"[WEIGHT] User {user_id} - TOTAL TIME: {total_time:.3f}s")

            return weights

        except Exception as e:
            logger.error(f"[WEIGHT] User {user_id} - ERROR: {str(e)}", exc_info=True)
            raise