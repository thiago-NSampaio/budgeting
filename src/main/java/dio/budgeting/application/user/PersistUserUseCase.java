package dio.budgeting.application.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dio.budgeting.application.input.PersistUserInput;
import dio.budgeting.application.output.UserOutput;
import dio.budgeting.domain.user.User;
import dio.budgeting.domain.user.UserRepository;

@Service
public class PersistUserUseCase {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public PersistUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserOutput execute(PersistUserInput input) {
        var passwordHash = passwordEncoder.encode(input.password());

        var user = userRepository.save(new User(input.name(), input.email(), passwordHash));

        return UserOutput.from(user);
    }
}
