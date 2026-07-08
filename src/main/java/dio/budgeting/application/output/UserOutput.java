package dio.budgeting.application.output;

import java.time.LocalDateTime;

import dio.budgeting.domain.user.User;

public record UserOutput(String id, String name, String email,String password, LocalDateTime createdAt) {
    public static UserOutput from(User user){
        return new UserOutput(
            user.getId().uuid().toString(),
            user.getName(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getCreatedAt()
        );
    }
}
