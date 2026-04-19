package scrollic.news_handler_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import scrollic.news_handler_service.dto.NewsArticle;
import scrollic.news_handler_service.entity.NewsByDateEntity;
import scrollic.news_handler_service.entity.NewsByThemeAndPopularityEntity;
import scrollic.news_handler_service.entity.NewsEntity;
import scrollic.news_handler_service.repository.NewsByDateRepository;
import scrollic.news_handler_service.repository.NewsByThemeAndPopularityRepository;
import scrollic.news_handler_service.repository.NewsRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class NewsProcessorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsProcessorService.class);

    private final NewsRepository newsRepository;
    private final NewsByDateRepository newsByDateRepository;
    private final NewsByThemeAndPopularityRepository newsByThemeAndPopularityRepository;

    private static final DateTimeFormatter DATE_BUCKET_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public NewsProcessorService(
            NewsRepository newsRepository,
            NewsByDateRepository newsByDateRepository,
            NewsByThemeAndPopularityRepository newsByThemeAndPopularityRepository) {
        this.newsRepository = newsRepository;
        this.newsByDateRepository = newsByDateRepository;
        this.newsByThemeAndPopularityRepository = newsByThemeAndPopularityRepository;
    }

    public Mono<Void> processAndSave(NewsArticle article) {

        UUID id = generateUuidFromUrl(article.getUrl());

        return Mono.when(
                        saveToNewsTable(article, id),
                        saveToNewsByDateTable(article, id),
                        saveToNewsByThemeAndPopularityTable(article, id)
                )
                .doOnSuccess(v ->
                        LOGGER.info("Новость id = {} сохранена во все таблицы Cassandra", id))
                .doOnError(error ->
                        LOGGER.error("Ошибка сохранения новости id = {} в Cassandra: {}",
                                id, error.getMessage()));
    }

    private Mono<NewsEntity> saveToNewsTable(NewsArticle article, UUID id) {
        return Mono.fromCallable(() -> mapToNewsEntity(article, id))
                .flatMap(newsRepository::save)
                .doOnSuccess(entity -> LOGGER.info("Сохранено в news, id = {}", id))
                .doOnError(error ->
                        LOGGER.error("Ошибка при сохранении в news: {}", error.getMessage()));
    }

    private Mono<NewsByDateEntity> saveToNewsByDateTable(NewsArticle article, UUID id) {
        return Mono.fromCallable(() -> mapToNewsByDateEntity(article, id))
                .flatMap(newsByDateRepository::save)
                .doOnSuccess(entity ->
                        LOGGER.info("Сохранено в news_by_date, id = {}", id))
                .doOnError(error ->
                        LOGGER.error("Ошибка при сохранении в news_by_date: {}", error.getMessage()));
    }

    private Mono<NewsByThemeAndPopularityEntity> saveToNewsByThemeAndPopularityTable
            (NewsArticle article, UUID id) {
        return Mono.fromCallable(() -> mapToNewsByThemeAndPopularityEntity(article, id))
                .flatMap(newsByThemeAndPopularityRepository::save)
                .doOnSuccess(entity ->
                        LOGGER.info("Сохранено в news_by_theme_and_popularity, id = {}", id))
                .doOnError(error ->
                        LOGGER.error("Ошибка при сохранении в news_by_theme_and_popularity: {}", error.getMessage()));
    }

    private NewsEntity mapToNewsEntity(NewsArticle article, UUID id) {
        NewsEntity entity = new NewsEntity();

        entity.setId(id);

        entity.setHead(article.getTitle());
        entity.setSummary(article.getDescription());
        entity.setText(article.getContent());
        entity.setUrl(article.getUrl());
        entity.setUrlPicture(article.getImage());
        entity.setCreatedAt(article.getPublishedAt());

        entity.setPopularity(0);
        entity.setThemeId(-1);

        return entity;
    }

    private NewsByDateEntity mapToNewsByDateEntity(NewsArticle article, UUID id) {
        NewsByDateEntity entity = new NewsByDateEntity();

        entity.setDateBucket(formatDateBucket(article.getPublishedAt()));
        entity.setCreatedAt(article.getPublishedAt());
        entity.setId(id);

        entity.setThemeId(-1);
        entity.setPopularity(0);

        entity.setHead(article.getTitle());
        entity.setSummary(article.getDescription());
        entity.setUrl(article.getUrl());
        return entity;
    }

    private NewsByThemeAndPopularityEntity mapToNewsByThemeAndPopularityEntity(
            NewsArticle article, UUID id) {
        NewsByThemeAndPopularityEntity entity = new NewsByThemeAndPopularityEntity();

        entity.setThemeId(-1);
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

    private UUID generateUuidFromUrl(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(url.getBytes(StandardCharsets.UTF_8));
            return UUID.nameUUIDFromBytes(digest);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("MD5 не доступен, был использован случайный UUID");
            return UUID.randomUUID();
        }
    }

    private String formatDateBucket(Instant instant) {
        return LocalDate.ofInstant(instant, ZoneOffset.UTC)
                .format(DATE_BUCKET_FORMATTER);
    }
}
