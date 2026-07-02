package dio.budgeting.application;

import org.springframework.stereotype.Service;

import dio.budgeting.application.output.BudgetLimitOutput;
import dio.budgeting.domain.BudgetLimitRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class ListBudgetLimitUseCase {
    private final BudgetLimitRepository budgetLimitRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ListBudgetLimitUseCase(BudgetLimitRepository budgetLimitRepository,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.budgetLimitRepository = budgetLimitRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public BudgetLimitOutput execute() {
        var userId = authenticatedUserProvider.currentUserId();

        var budgetLimit = budgetLimitRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Budget limit not found"));

        return BudgetLimitOutput.from(budgetLimit);
    }
}
