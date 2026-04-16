package scrollic.news_fetcher_service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import scrollic.news_fetcher_service.client.GNewsClient;
import scrollic.news_fetcher_service.dto.NewsArticle;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NewsFetcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsFetcherService.class);

    private final Set<String> sentUrls = ConcurrentHashMap.newKeySet();

    @Autowired
    private GNewsClient gNewsClient;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Scheduled(fixedDelayString = "${news.fetch-interval}")
    public void publishNews() {
        LOGGER.info("Запланированный вызов NewsFetcherService.");

        List<NewsArticle> articles = gNewsClient.getNewsByAllCategories();
        LOGGER.info("Получено: {} новостей", articles.size());

        List<NewsArticle> filteredArticles = articles.stream()
                .filter(article -> !sentUrls.contains(article.getUrl()))
                .toList();
        LOGGER.info("Осталось после дедуплицирования: {} новостей", filteredArticles.size());

        if(filteredArticles.isEmpty()) {
            LOGGER.info("Новых новостей нет, в Kafka ничего не отправлено");
        }
        else {
            kafkaProducerService.sendNewsBatch(filteredArticles);

            for (NewsArticle article : filteredArticles) {
                sentUrls.add(article.getUrl());
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void clearSentUrlsCache() {
        this.sentUrls.clear();
        LOGGER.info("Кеш обработанных URL очищен");
    }
}
