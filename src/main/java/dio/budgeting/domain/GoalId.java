package dio.budgeting.domain;

import java.util.UUID;

public record GoalId(UUID uuid) {
    public GoalId(){
        this(UUID.randomUUID());
    }
}
