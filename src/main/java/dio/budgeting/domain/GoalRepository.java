package dio.budgeting.domain;

import java.util.Optional;

public interface GoalRepository {
    Goal save(Goal goal);
    Optional<Goal> findByUserId(UserId userId);
}
