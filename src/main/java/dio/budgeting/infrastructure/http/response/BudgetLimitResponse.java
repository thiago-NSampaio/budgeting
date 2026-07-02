package dio.budgeting.infrastructure.http.response;

import java.time.YearMonth;

import dio.budgeting.application.output.BudgetLimitOutput;

public record BudgetLimitResponse(String id, YearMonth month, Long limitAmount) {
    public static BudgetLimitResponse from(BudgetLimitOutput output){
        return new BudgetLimitResponse(output.id(), output.month(), output.limitAmount());
    }
}