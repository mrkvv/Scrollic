from cassandra.cluster import Session
from datetime import datetime, timedelta
from typing import List, Dict
import logging
from app.schemas import NewsItem

logger = logging.getLogger(__name__)


class FeedService:
    def __init__(self, cassandra_session: Session):
        self.session = cassandra_session

    async def get_user_theme_weights(self, user_id: int) -> Dict[int, float]:
        """Получение весов тем пользователя"""
        query = "SELECT theme_id, weight FROM user_theme_weights WHERE user_id = %s"
        rows = self.session.execute(query, (user_id,))

        weights = {}
        for row in rows:
            weights[row['theme_id']] = float(row['weight'])

        logger.info(f"User {user_id} has {len(weights)} theme weights")
        return weights

    async def get_recent_news(self, days: int = 7) -> List[Dict]:
        """Получение новостей за последние N дней"""
        query = """
            SELECT id, head, summary, text, url, url_picture, popularity, theme_id, created_at
            FROM news
            WHERE created_at >= %s
            ALLOW FILTERING
        """
        seven_days_ago = datetime.now() - timedelta(days=days)
        rows = self.session.execute(query, (seven_days_ago,))

        news_list = []
        for row in rows:
            news_list.append(dict(row))

        logger.info(f"Found {len(news_list)} recent news")
        return news_list

    async def get_seen_news(self, user_id: int) -> set:
        """Получение ID просмотренных новостей"""
        query = "SELECT news_id FROM user_seen WHERE user_id = %s"
        rows = self.session.execute(query, (user_id,))

        seen_ids = {str(row['news_id']) for row in rows}
        logger.info(f"User {user_id} has seen {len(seen_ids)} news")
        return seen_ids

    async def get_liked_news(self, user_id: int) -> set:
        """Получение ID лайкнутых новостей"""
        query = "SELECT news_id FROM user_likes WHERE user_id = %s"
        rows = self.session.execute(query, (user_id,))

        liked_ids = {str(row['news_id']) for row in rows}
        logger.info(f"User {user_id} has liked {len(liked_ids)} news")
        return liked_ids

    async def get_personalized_feed(self, user_id: int, limit: int) -> List[NewsItem]:
        """Формирование персонализированной ленты"""
        weights = await self.get_user_theme_weights(user_id)

        if not weights:
            weights = {1: 1.0, 2: 1.0, 3: 1.0, 4: 1.0, 5: 1.0}
            logger.info(f"No weights for user {user_id}, using default weights")

        all_news = await self.get_recent_news()

        if not all_news:
            logger.warning(f"No news found for user {user_id}")
            return []

        seen_news = await self.get_seen_news(user_id)
        liked_news = await self.get_liked_news(user_id)
        excluded_news = seen_news.union(liked_news)

        logger.info(f"Excluded {len(excluded_news)} news for user {user_id}")

        news_by_theme = {}
        for news in all_news:
            news_id = str(news['id'])

            if news_id in excluded_news:
                continue

            theme_id = news['theme_id']
            if theme_id not in news_by_theme:
                news_by_theme[theme_id] = []
            news_by_theme[theme_id].append(news)

        for theme_id in news_by_theme:
            news_by_theme[theme_id].sort(key=lambda x: x['popularity'], reverse=True)

        selected_news = []
        total_weight = sum(weights.values())

        for theme_id, weight in weights.items():
            if theme_id in news_by_theme:
                theme_news = news_by_theme[theme_id]
                news_count = max(1, int(limit * weight / total_weight))
                selected_news.extend(theme_news[:news_count])
                logger.info(f"Theme {theme_id}: weight={weight}, news_count={news_count}")

        selected_news.sort(key=lambda x: x['popularity'], reverse=True)

        selected_news = selected_news[:limit]

        feed = []
        for news in selected_news:
            try:
                feed.append(NewsItem(
                    id=news['id'],
                    head=news.get('head', ''),
                    summary=news.get('summary', ''),
                    text=news.get('text', ''),
                    url=news.get('url'),
                    url_picture=news.get('url_picture'),
                    popularity=news.get('popularity', 0),
                    theme_id=news.get('theme_id', 0),
                    created_at=news['created_at']
                ))
            except Exception as e:
                logger.error(f"Error converting news {news.get('id')}: {e}")
                continue

        logger.info(f"Generated feed with {len(feed)} news for user {user_id}")
        return feed