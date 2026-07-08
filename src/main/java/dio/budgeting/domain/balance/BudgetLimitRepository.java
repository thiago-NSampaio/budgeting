package dio.budgeting.domain.balance;

import java.util.Optional;

import dio.budgeting.domain.user.UserId;

public interface BudgetLimitRepository {
    BudgetLimit save(BudgetLimit budgetLimit);
    Optional<BudgetLimit> findByUserId(UserId userId);
}
