package dio.budgeting.domain;

import java.util.List;

import dio.budgeting.application.output.TransactionOutput;
import lombok.Getter;

@Getter
public class Dashboard {
    private Long balance;
    private Long income;
    private Long expense;
    private BudgetLimit budget;
    private Goal goal;
    private List<TransactionOutput> lastTransactions;
    private String aiInsight;

    public Dashboard(Long balance, Long income, Long expense, BudgetLimit budget, Goal goal,
            List<TransactionOutput> lastTransactions, String aiInsight) {
        this.balance = balance;
        this.income = income;
        this.expense = expense;
        this.budget = budget;
        this.goal = goal;
        this.lastTransactions = lastTransactions;
        this.aiInsight = aiInsight;
    }
}
