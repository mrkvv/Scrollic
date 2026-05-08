package scrollic.news_handler_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import scrollic.news_handler_service.dto.NewsArticle;
import scrollic.news_handler_service.entity.NewsByDateEntity;
import scrollic.news_handler_service.entity.NewsByThemeAndPopularityEntity;
import scrollic.news_handler_service.entity.NewsEntity;
import scrollic.news_handler_service.repository.NewsByDateRepository;
import scrollic.news_handler_service.repository.NewsByThemeAndPopularityRepository;
import scrollic.news_handler_service.repository.NewsRepository;
import scrollic.news_handler_service.util.NewsUtils;

import java.util.UUID;

@Service
public class NewsProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsProcessorService.class);

    private final NewsRepository newsRepository;
    private final NewsByDateRepository newsByDateRepository;
    private final NewsByThemeAndPopularityRepository newsByThemeAndPopularityRepository;

    private final NewsTaggerService newsTaggerService;

    public NewsProcessorService(
            NewsRepository newsRepository,
            NewsByDateRepository newsByDateRepository,
            NewsByThemeAndPopularityRepository newsByThemeAndPopularityRepository,
            NewsTaggerService newsTaggerService) {
        this.newsRepository = newsRepository;
        this.newsByDateRepository = newsByDateRepository;
        this.newsByThemeAndPopularityRepository = newsByThemeAndPopularityRepository;
        this.newsTaggerService = newsTaggerService;
    }

    public Mono<Void> processAndSave(NewsArticle article) {

        UUID id = NewsUtils.generateUuidFromUrl(article.getUrl());
        int themeId = newsTaggerService.tagNews(article);

        return Mono.when(
                        saveToNewsTable(article, id, themeId),
                        saveToNewsByDateTable(article, id, themeId),
                        saveToNewsByThemeAndPopularityTable(article, id, themeId)
                )
                .doOnSuccess(v ->
                        LOGGER.info("Новость id = {} сохранена во все таблицы Cassandra", id))
                .doOnError(error ->
                        LOGGER.error("Ошибка сохранения новости id = {} в Cassandra: {}",
                                id, error.getMessage()));
    }

    private Mono<NewsEntity> saveToNewsTable(NewsArticle article, UUID id, int themeId) {
        return Mono.fromCallable(() -> mapToNewsEntity(article, id, themeId))
                .flatMap(newsRepository::save)
                .doOnSuccess(entity -> LOGGER.info("Сохранено в news, id = {}", id))
                .doOnError(error ->
                        LOGGER.error("Ошибка при сохранении в news: {}", error.getMessage()));
    }

    private Mono<NewsByDateEntity> saveToNewsByDateTable(
            NewsArticle article, UUID id, int themeId) {
        return Mono.fromCallable(() -> mapToNewsByDateEntity(article, id, themeId))
                .flatMap(newsByDateRepository::save)
                .doOnSuccess(entity ->
                        LOGGER.info("Сохранено в news_by_date, id = {}", id))
                .doOnError(error ->
                        LOGGER.error("Ошибка при сохранении в news_by_date: {}", error.getMessage()));
    }

    private Mono<NewsByThemeAndPopularityEntity> saveToNewsByThemeAndPopularityTable
            (NewsArticle article, UUID id, int themeId) {
        return Mono.fromCallable(() -> mapToNewsByThemeAndPopularityEntity(article, id, themeId))
                .flatMap(newsByThemeAndPopularityRepository::save)
                .doOnSuccess(entity ->
                        LOGGER.info("Сохранено в news_by_theme_and_popularity, id = {}", id))
                .doOnError(error ->
                        LOGGER.error("Ошибка при сохранении в news_by_theme_and_popularity: {}", error.getMessage()));
    }

    private NewsEntity mapToNewsEntity(NewsArticle article, UUID id, int themeId) {
        NewsEntity entity = new NewsEntity();

        entity.setId(id);

        entity.setHead(article.getTitle());
        entity.setSummary(article.getDescription());
        entity.setText(article.getContent());
        entity.setUrl(article.getUrl());
        entity.setUrlPicture(article.getImage());
        entity.setCreatedAt(article.getPublishedAt());

        entity.setPopularity(0);
        entity.setThemeId(themeId);

        return entity;
    }

    private NewsByDateEntity mapToNewsByDateEntity(
            NewsArticle article, UUID id, int themeId) {

        NewsByDateEntity entity = new NewsByDateEntity();

        entity.setDateBucket(NewsUtils.getDateBucket(article.getPublishedAt()));
        entity.setCreatedAt(article.getPublishedAt());
        entity.setId(id);

        entity.setThemeId(themeId);
        entity.setPopularity(0);

        entity.setHead(article.getTitle());
        entity.setSummary(article.getDescription());
        entity.setUrl(article.getUrl());
        return entity;
    }

    private NewsByThemeAndPopularityEntity mapToNewsByThemeAndPopularityEntity(
            NewsArticle article, UUID id, int themeId) {

        NewsByThemeAndPopularityEntity entity = new NewsByThemeAndPopularityEntity();

        entity.setThemeId(themeId);
        entity.setPopularity(0);

        entity.setCreatedAt(article.getPublishedAt());
        entity.setId(id);
        entity.setHead(article.getTitle());
        entity.setSummary(article.getDescription());
        entity.setText(article.getContent());
        entity.setUrl(article.getUrl());
        entity.setUrlPicture(article.getImage());
        return entity;
    }
}
