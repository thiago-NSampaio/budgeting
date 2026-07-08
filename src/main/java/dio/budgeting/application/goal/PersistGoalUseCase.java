package dio.budgeting.application.goal;

import org.springframework.stereotype.Service;

import dio.budgeting.application.input.PersistGoalInput;
import dio.budgeting.application.output.GoalOutput;
import dio.budgeting.domain.goal.Goal;
import dio.budgeting.domain.goal.GoalRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class PersistGoalUseCase {
    private GoalRepository goalRepository;
    private AuthenticatedUserProvider authenticatedUserProvider;

    public PersistGoalUseCase(GoalRepository goalRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.goalRepository = goalRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public GoalOutput execute (PersistGoalInput input){
        var userId = authenticatedUserProvider.currentUserId();

        var goal = goalRepository.save(new Goal(userId, input.title(), input.targetAmount(), input.currentAmount(), input.deadline())); 

        return GoalOutput.from(goal);
    }
    
}