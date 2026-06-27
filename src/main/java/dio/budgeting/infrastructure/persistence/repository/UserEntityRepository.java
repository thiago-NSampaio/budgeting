package dio.budgeting.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import dio.budgeting.infrastructure.persistence.entity.UserEntity;

public interface UserEntityRepository extends CrudRepository<UserEntity, UUID>{
    Optional<UserEntity> findByEmail(String email);
}
