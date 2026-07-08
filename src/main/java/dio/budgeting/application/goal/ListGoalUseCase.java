package dio.budgeting.application.goal;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import dio.budgeting.application.output.GoalOutput;
import dio.budgeting.domain.goal.GoalRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class ListGoalUseCase {
    private final GoalRepository goalRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ListGoalUseCase(GoalRepository goalRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.goalRepository = goalRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Tool(name = "list-financial-goal" ,description = "Lista a meta financeira definida pelo usuário")
    public GoalOutput execute() {
        var userId = authenticatedUserProvider.currentUserId();

        var goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        return GoalOutput.from(goal);
    }
}
