package dio.budgeting.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import dio.budgeting.domain.user.User;
import dio.budgeting.domain.user.UserRepository;
import dio.budgeting.infrastructure.persistence.entity.UserEntity;

@Repository
public class JpaUserRepository implements UserRepository {
    private UserEntityRepository userEntityRepository;

    public JpaUserRepository(UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public User save(User user) {
        var entity = UserEntity.from(user);
        return userEntityRepository.save(entity).toDomain();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Optional<UserEntity> entity = userEntityRepository.findByEmail(email);

        return entity.map(UserEntity::toDomain);
    }
}
