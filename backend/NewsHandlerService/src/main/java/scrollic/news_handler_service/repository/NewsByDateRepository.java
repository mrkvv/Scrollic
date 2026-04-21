package scrollic.news_handler_service.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import scrollic.news_handler_service.entity.NewsByDateEntity;

import java.util.UUID;

public interface NewsByDateRepository extends ReactiveCassandraRepository<NewsByDateEntity, UUID> {
}
