package scrollic.news_fetcher_service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import scrollic.news_fetcher_service.dto.NewsArticle;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, NewsArticle> kafkaTemplate;

    @Value("${spring.kafka.topic.news}")
    private String newsTopic;

    public void sendNews(NewsArticle article) {
        String key = article.getUrl();

        CompletableFuture<SendResult<String, NewsArticle>> future =
                kafkaTemplate.send(newsTopic, key, article);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                LOGGER.error("Ошибка в отправке сообщения в Kafka. Url новости={}, error={}",
                        article.getUrl(), ex.getMessage(), ex);
            } else {
                LOGGER.debug("Новость url={} отправлена в Kafka. Offset={}, partition={}",
                        article.getUrl(),
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().partition());
            }
        });
    }

    public void sendNewsBatch(List<NewsArticle> articles) {
        articles.forEach(this::sendNews);
    }
}
