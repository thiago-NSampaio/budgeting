package dio.budgeting.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import dio.budgeting.domain.goal.Goal;
import dio.budgeting.domain.goal.GoalRepository;
import dio.budgeting.domain.user.UserId;
import dio.budgeting.infrastructure.persistence.entity.GoalEntity;

@Repository
public class JpaGoalRepository implements GoalRepository{

    private GoalEntityRepository goalEntityRepository;

    public JpaGoalRepository(GoalEntityRepository goalEntityRepository) {
        this.goalEntityRepository = goalEntityRepository;
    }

    @Override
    public Goal save(Goal goal) {
        var entity = GoalEntity.from(goal);
        return goalEntityRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Goal> findByUserId(UserId userId) {
        Optional<GoalEntity> entity = goalEntityRepository.findByUserId(userId.uuid());
        return entity.map(GoalEntity::toDomain);
    }
    
}
