package dio.budgeting.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import dio.budgeting.infrastructure.persistence.entity.GoalEntity;


public interface GoalEntityRepository extends CrudRepository<GoalEntity, UUID> {
    Optional<GoalEntity> findByUserId(UUID userId);
}
