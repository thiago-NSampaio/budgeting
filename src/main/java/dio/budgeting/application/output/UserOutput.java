package dio.budgeting.application.output;

import dio.budgeting.domain.User;

public record UserOutput(String id, String name, String email,String password) {
    public static UserOutput from(User user){
        return new UserOutput(
            user.getId().uuid().toString(),
            user.getName(),
            user.getEmail(),
            user.getPasswordHash()
        );
    }
}
