package dio.budgeting.domain.goal;

import java.util.UUID;

public record GoalId(UUID uuid) {
    public GoalId(){
        this(UUID.randomUUID());
    }
}
