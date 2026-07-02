package dio.budgeting.application;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import dio.budgeting.application.output.GoalOutput;
import dio.budgeting.domain.GoalRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class ListGoalUseCase {
    private final GoalRepository goalRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;


    public ListGoalUseCase(GoalRepository goalRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.goalRepository = goalRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Tool(name = "list-goal-" ,description = "Lista o limite de orçamento defino pelo usuário")
    public GoalOutput execute() {
        var userId = authenticatedUserProvider.currentUserId();

        var goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        return GoalOutput.from(goal);
    }
}
