package dio.budgeting.infrastructure.http.response;

import dio.budgeting.application.output.UserOutput;

public record UserResponse(String id, String name, String email, String password) {
    public static UserResponse from(UserOutput output) {
        return new UserResponse(
                output.id(),
                output.name(),
                output.email(),
                output.password());
    }
}
