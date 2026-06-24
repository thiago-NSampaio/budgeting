package dio.budgeting.infrastructure.persistence.repository;

import dio.budgeting.domain.User;
import dio.budgeting.domain.UserRepository;
import dio.budgeting.infrastructure.persistence.entity.UserEntity;

public class JpaUserRepository implements UserRepository{
    private UserEntityRepository userEntityRepository;

    public JpaUserRepository(UserEntityRepository userEntityRepository){
        this.userEntityRepository = userEntityRepository;
    }

    @Override
    public User save(User user) {
        var entity = UserEntity.from(user);
        return userEntityRepository.save(entity).toDomain();
    }
}
