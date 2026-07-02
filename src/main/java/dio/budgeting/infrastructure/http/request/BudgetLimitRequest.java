package dio.budgeting.infrastructure.http.request;

import java.time.YearMonth;

import dio.budgeting.application.input.PersistBudgetLimitInput;

public record BudgetLimitRequest(YearMonth month, Long limitAmount) {
    public PersistBudgetLimitInput toInput(){
        return new PersistBudgetLimitInput(month, limitAmount);
    }
}
