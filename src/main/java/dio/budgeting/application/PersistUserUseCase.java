package dio.budgeting.application;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dio.budgeting.application.input.PersistUserInput;
import dio.budgeting.application.output.UserOutput;
import dio.budgeting.domain.User;
import dio.budgeting.domain.UserRepository;

@Service
public class PersistUserUseCase {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public PersistUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Tool(name = "persist-user", description = "Persiste um novo usuário para usar a aplicação")
    public UserOutput execute(PersistUserInput input) {
        var user = userRepository.save(new User(input.name(), input.email(), input.password()));

        var passwordHash = passwordEncoder.encode(input.password());

        user.changePassword(passwordHash);

        return UserOutput.from(user);
    }
}
