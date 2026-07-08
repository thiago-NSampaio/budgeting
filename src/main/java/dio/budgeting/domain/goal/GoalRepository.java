package dio.budgeting.domain.goal;

import java.util.Optional;

import dio.budgeting.domain.user.UserId;

public interface GoalRepository {
    Goal save(Goal goal);
    Optional<Goal> findByUserId(UserId userId);
}
