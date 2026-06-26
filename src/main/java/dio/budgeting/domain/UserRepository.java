package dio.budgeting.domain;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findUserByEmail(Email email);    
}
