package scrollic.news_fetcher_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import scrollic.news_fetcher_service.dto.GNewsResponse;
import scrollic.news_fetcher_service.dto.NewsArticle;
import scrollic.news_fetcher_service.model.GNewsCategory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class GNewsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GNewsClient.class);

    private final WebClient webClient;

    @Value("${news.apikey}")
    private String apiKey;

    @Value("${news.retry.max-attempts}")
    private int maxRetryAttempts;

    @Value("${news.retry.initial-delay-ms}")
    private long initialDelay;

    public GNewsClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30))
                .keepAlive(false);

        this.webClient = WebClient.builder()
                .baseUrl("https://gnews.io/api/v4")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public List<NewsArticle> getNewsByCategory(GNewsCategory category) {
        String fromDate = Instant.now()
                .minus(24, ChronoUnit.HOURS)
                .toString();

        long delay = initialDelay;

        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                LOGGER.info("Получение новостей для категории {}, попытка {}",
                        category.getValue(), attempt);

                GNewsResponse response = getResponse(category, fromDate);

                if (response != null && response.getArticles() != null) {
                    LOGGER.info("Категория {}: получено {} статей",
                            category.getValue(), response.getArticles().size());
                    return response.getArticles();
                }
            }
            catch(Exception e) {
                LOGGER.warn("Категория {}: ошибка при попытке {}/{}: {}",
                        category.getValue(), attempt, maxRetryAttempts, e.getMessage());

                if(attempt < maxRetryAttempts) {
                    try {
                        LOGGER.info("Ожидание {} мс перед следующей попыткой", delay);
                        Thread.sleep(delay);
                        delay *= 2;
                    }
                    catch(InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        LOGGER.error("Ожидание прервано, прекращаем попытки для категории {}",
                                category.getValue());
                        break;
                    }
                }
            }
        }

        LOGGER.error("Категория {}: исчерпаны все попытки, пропуск", category.getValue());
        return List.of();
    }

    private GNewsResponse getResponse(GNewsCategory category, String fromDate) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/top-headlines")
                        .queryParam("lang", "ru")
                        .queryParam("country", "ru")
                        .queryParam("topic", category.getValue())
                        .queryParam("from", fromDate)
                        .queryParam("sortby", "publishedAt")
                        .queryParam("expand", "content")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GNewsResponse.class)
                .block();
    }

    public List<NewsArticle> getNewsByAllCategories() {
        return GNewsCategory.getAllCategories().stream()
                .flatMap(category -> {
                    List<NewsArticle> articles = getNewsByCategory(category);
                    try {
                        Thread.sleep(1100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.error("Ожидание между запросами было прервано {}", e.getMessage());
                    }
                    return articles.stream();
                })
                .toList();
    }
}
