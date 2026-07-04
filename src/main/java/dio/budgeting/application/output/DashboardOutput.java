package dio.budgeting.application.output;

import java.util.List;

public record DashboardOutput(
    Long balance,
    Long income,
    Long expense,
    BudgetLimitOutput budget,
    GoalOutput goal,
    List<TransactionOutput> lastTransactions,
    String aiInsight
) {
}