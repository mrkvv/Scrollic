package scrollic.news_handler_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import scrollic.news_handler_service.dto.NewsArticle;
import scrollic.news_handler_service.service.NewsProcessorService;

@Component
public class NewsConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewsConsumer.class);

    private final NewsProcessorService newsProcessorService;

    public NewsConsumer(NewsProcessorService newsProcessorService) {
        this.newsProcessorService = newsProcessorService;
    }

    @KafkaListener(topics = "${spring.kafka.topic.news}")
    public void consume(NewsArticle article, Acknowledgment acknowledgment) {
        LOGGER.info("Получена новость: {}", article.getUrl());

        try {
            newsProcessorService.processAndSave(article).block();
            LOGGER.info("Новость {} успешно обработана", article.getUrl());
            acknowledgment.acknowledge();
        }
        catch (Exception e) {
            LOGGER.error("Ошибка обработки новости {}: {}", article.getUrl(), e.getMessage());
        }
    }

}
