package scrollic.news_handler_service.repository;


import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import scrollic.news_handler_service.entity.NewsEntity;

import java.util.UUID;

public interface NewsRepository extends ReactiveCassandraRepository<NewsEntity, UUID> {
}
