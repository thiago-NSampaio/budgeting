package dio.budgeting.domain;

import java.util.Optional;

public interface BudgetLimitRepository {
    BudgetLimit save(BudgetLimit budgetLimit);
    Optional<BudgetLimit> findByUserId(UserId userId);
}
