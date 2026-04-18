package scrollic.news_fetcher_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import scrollic.news_fetcher_service.client.exception.GNewsException;
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

        try {
            GNewsResponse response = requestNewsAsync(category, fromDate).block();
            if (response != null && response.getArticles() != null) {
                LOGGER.info("Категория {}: получено {} статей",
                        category.getValue(), response.getArticles().size());
                return response.getArticles();
            }
        }
        catch (GNewsException e) {
            LOGGER.error("Категория {}: {}", category.getValue(), e.getMessage());
        }
        catch (Exception e) {
            LOGGER.error("Ошибка: {}", e.getMessage());
        }
        return List.of();
    }

    private Mono<GNewsResponse> requestNewsAsync(GNewsCategory category, String fromDate) {
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
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new GNewsException("Ошибка сервера", 500)))
                .onStatus(status -> (status == HttpStatus.TOO_MANY_REQUESTS),
                        response -> Mono.error(new GNewsException("TOO_MANY_REQUESTS", 429)))
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(
                                new GNewsException("Ошибка клиента " + response.statusCode(),
                                        response.statusCode().value())))
                .bodyToMono(GNewsResponse.class)
                .retryWhen(createRetryStrategy(category));
    }

    private Retry createRetryStrategy(GNewsCategory category) {
        return Retry.backoff(maxRetryAttempts, Duration.ofMillis(initialDelay))
                .filter(this::isRetryableError)
                .doBeforeRetry(retrySignal ->
                        LOGGER.info("Категория {} retry #{} по причине: {}",
                                category.getValue(), retrySignal.totalRetries() + 1,
                                retrySignal.failure().getMessage()))
                .onRetryExhaustedThrow((spec, signal) ->
                        new GNewsException("Исчерпаны все попытки", -1));
    }

    private boolean isRetryableError(Throwable throwable) {
        if(throwable instanceof GNewsException ex) {
            return ex.isRetryable();
        }

        // сетевые ошибки
        return throwable instanceof java.net.SocketException
                || throwable instanceof java.net.SocketTimeoutException
                || throwable instanceof java.util.concurrent.TimeoutException;
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
