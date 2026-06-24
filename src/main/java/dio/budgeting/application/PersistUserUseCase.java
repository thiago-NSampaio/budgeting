package dio.budgeting.application;

import org.springframework.ai.tool.annotation.Tool;

import dio.budgeting.application.input.PersistUserInput;
import dio.budgeting.application.output.UserOutput;
import dio.budgeting.domain.User;
import dio.budgeting.domain.UserRepository;

public class PersistUserUseCase {
    private UserRepository userRepository;

    public PersistUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Tool(name = "persist-user", description = "Persiste uma novo usuário")
    public UserOutput execute(PersistUserInput input) {
        var user = userRepository.save(new User(input.name(), input.email(), input.password()));
        return UserOutput.from(user);
    }
}
