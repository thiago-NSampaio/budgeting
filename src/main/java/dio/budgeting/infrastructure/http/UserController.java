package dio.budgeting.infrastructure.http;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.user.PersistUserUseCase;
import dio.budgeting.infrastructure.http.request.UserRequest;
import dio.budgeting.infrastructure.http.response.UserResponse;

@RestController
@RequestMapping("/user")
public class UserController {
    private PersistUserUseCase persistUserUseCase;
    
    public UserController(PersistUserUseCase persistUserUseCase){
        this.persistUserUseCase = persistUserUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody UserRequest request){
        var user = persistUserUseCase.execute(request.toInput());
        return UserResponse.from(user);
    }
}
