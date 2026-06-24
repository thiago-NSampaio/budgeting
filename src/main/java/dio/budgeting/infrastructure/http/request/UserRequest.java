package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.PersistUserInput;

public record UserRequest(String name, String email, String password) {
    public PersistUserInput toInput(){
        return new PersistUserInput(name,email,password);
    }
}
