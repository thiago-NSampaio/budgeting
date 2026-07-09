package dio.budgeting.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import dio.budgeting.infrastructure.persistence.entity.ChatMemoryEntity;

public interface ChatMemoryEntityRepository extends CrudRepository<ChatMemoryEntity,UUID> {
    List<ChatMemoryEntity> findByUserId(UUID userId);
}
