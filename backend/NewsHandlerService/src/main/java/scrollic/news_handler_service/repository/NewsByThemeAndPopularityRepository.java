package scrollic.news_handler_service.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import scrollic.news_handler_service.entity.NewsByThemeAndPopularityEntity;

import java.util.UUID;

public interface NewsByThemeAndPopularityRepository
        extends ReactiveCassandraRepository<NewsByThemeAndPopularityEntity, UUID> {
}
