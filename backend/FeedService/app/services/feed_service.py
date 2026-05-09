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
        print(f"=== START get_personalized_feed for user {user_id}, limit {limit} ===")

        weights = await self.get_user_theme_weights(user_id)
        print(f"Step 1 - All weights: {weights}")

        if not weights:
            weights = {1: 1.0, 2: 1.0, 3: 1.0, 4: 1.0, 5: 1.0}
            print(f"No weights for user {user_id}, using default weights: {weights}")

        all_news = await self.get_recent_news()
        print(f"Step 2 - Total news from Cassandra: {len(all_news)}")

        if not all_news:
            print(f"No news found for user {user_id}")
            return []

        seen_news = await self.get_seen_news(user_id)
        liked_news = await self.get_liked_news(user_id)
        excluded_news = seen_news.union(liked_news)
        print(f"Step 3 - Excluded news: {len(excluded_news)}")

        # Фильтруем новости
        news_by_theme = {}
        for news in all_news:
            news_id = str(news['id'])
            if news_id in excluded_news:
                continue
            theme_id = news.get('theme_id', -1)
            if theme_id not in news_by_theme:
                news_by_theme[theme_id] = []
            news_by_theme[theme_id].append(news)

        print(f"Step 4 - News by theme (after exclude):")
        for theme_id, news_list in news_by_theme.items():
            print(f"  Theme {theme_id}: {len(news_list)} news")

        # Сортируем по популярности
        for theme_id in news_by_theme:
            news_by_theme[theme_id].sort(key=lambda x: x.get('popularity', 0), reverse=True)

        # Выбираем ТОЛЬКО темы, в которых есть новости
        active_themes = {theme_id: weight for theme_id, weight in weights.items()
                         if theme_id in news_by_theme}

        if not active_themes:
            print(f"No active themes with news!")
            return []

        print(f"Step 5 - Active themes (with news): {active_themes}")

        # Пересчитываем total_weight только для активных тем
        total_active_weight = sum(active_themes.values())
        print(f"Step 6 - Total active weight: {total_active_weight}")

        selected_news = []
        for theme_id, weight in active_themes.items():
            theme_news = news_by_theme[theme_id]
            # Распределяем limit пропорционально весам среди активных тем
            news_count = max(1, int(limit * weight / total_active_weight))
            selected_news.extend(theme_news[:news_count])
            print(f"  Theme {theme_id}: weight={weight}, news_count={news_count}, available={len(theme_news)}")

        # Обрезаем до limit (на случай перебора из-за округления)
        selected_news = selected_news[:limit]
        print(f"Step 7 - Selected {len(selected_news)} news before global sort")

        # Сортируем глобально по популярности
        selected_news.sort(key=lambda x: x.get('popularity', 0), reverse=True)

        print(f"Step 8 - Final result: {len(selected_news)} news")

        # Конвертируем в Pydantic модели
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
                print(f"Error converting news {news.get('id')}: {e}")
                continue

        print(f"=== END: Generated {len(feed)} news for user {user_id} ===")
        return feed