package scrollic.news_handler_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;
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

        newsProcessorService.processAndSave(article)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        v -> {
                            LOGGER.info("Новость {} успешно обработана", article.getUrl());
                            acknowledgment.acknowledge();
                        },
                        error -> {
                            LOGGER.error("Ошибка обработки новости {}: {}",
                                    article.getUrl(), error.getMessage());
                        }
                );
    }

}
