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
        LOGGER.info("Получение новостей для категории {}", category.getValue());

        String fromDate = Instant.now()
                .minus(24, ChronoUnit.HOURS)
                .toString();

        GNewsResponse response = webClient.get()
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

        return (response != null) ? response.getArticles() : List.of();
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
