package dio.budgeting.application.output;

import dio.budgeting.domain.User;

public record UserOutput(String id, String name, String email) {
    public static UserOutput from(User user){
        return new UserOutput(
            user.getName(),
            user.getEmail(),
            user.getEmail()
        );
    }
}
