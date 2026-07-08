package dio.budgeting.application.budget;

import org.springframework.stereotype.Service;

import dio.budgeting.application.input.PersistBudgetLimitInput;
import dio.budgeting.application.output.BudgetLimitOutput;
import dio.budgeting.domain.balance.BudgetLimit;
import dio.budgeting.domain.balance.BudgetLimitRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class PersistBudgetLimitUseCase {
    private BudgetLimitRepository budgetLimitRepository;
    private AuthenticatedUserProvider authenticatedUserProvider;

    public PersistBudgetLimitUseCase(BudgetLimitRepository budgetLimitRepository, AuthenticatedUserProvider authenticatedUserProvider) {
        this.budgetLimitRepository = budgetLimitRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public BudgetLimitOutput execute(PersistBudgetLimitInput input){
        var userId = authenticatedUserProvider.currentUserId();
        
        var budgetLimit = budgetLimitRepository.save(new BudgetLimit(userId, input.month(), input.limitAmount()));

        return BudgetLimitOutput.from(budgetLimit);
    }
}
