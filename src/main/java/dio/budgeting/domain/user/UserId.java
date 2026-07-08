package dio.budgeting.domain.user;

import java.util.UUID;

public record UserId(UUID uuid) {
    public UserId(){
        this(UUID.randomUUID());
    }
}
