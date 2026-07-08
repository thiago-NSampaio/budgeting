package dio.budgeting.application.output;

import java.time.YearMonth;

import dio.budgeting.domain.balance.BudgetLimit;

public record BudgetLimitOutput(String id, YearMonth month, Long limitAmount) {
    public static BudgetLimitOutput from(BudgetLimit budgetLimit){
        return new BudgetLimitOutput(
            budgetLimit.getId().uuid().toString(),
            budgetLimit.getMonth(),
            budgetLimit.getLimitAmount()
        );
    }
}
