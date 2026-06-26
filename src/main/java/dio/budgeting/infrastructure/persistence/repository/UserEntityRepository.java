package dio.budgeting.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import dio.budgeting.domain.Email;
import dio.budgeting.domain.User;
import dio.budgeting.infrastructure.persistence.entity.UserEntity;

public interface UserEntityRepository extends CrudRepository<UserEntity, UUID>{
    Optional<User> findUserByEmail(Email email);
}
