package dio.budgeting.application;

import org.springframework.stereotype.Service;

import dio.budgeting.application.output.GoalOutput;
import dio.budgeting.domain.GoalRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class listGoalUseCase {
    private final GoalRepository goalRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public listGoalUseCase(GoalRepository goalRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.goalRepository = goalRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public GoalOutput execute() {
        var userId = authenticatedUserProvider.currentUserId();

        var goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        return GoalOutput.from(goal);
    }
}
