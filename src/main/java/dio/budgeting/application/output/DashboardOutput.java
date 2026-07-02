package dio.budgeting.application.output;

import java.util.List;

import dio.budgeting.domain.BudgetLimit;
import dio.budgeting.domain.Dashboard;
import dio.budgeting.domain.Goal;

public record DashboardOutput(
        Long balance,
        Long income,
        Long expense,
        BudgetLimit budget,
        Goal goal,
        List<TransactionOutput> lastTransactions,
        String aiInsight) {
    public static DashboardOutput from(Dashboard dashboard){
        return new DashboardOutput(
            dashboard.getBalance(),dashboard.getIncome(), dashboard.getExpense(), dashboard.getBudget(), dashboard.getGoal(), dashboard.getLastTransactions(), dashboard.getAiInsight()
        );
    }
}
