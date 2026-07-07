package dio.budgeting.application;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import dio.budgeting.application.output.BudgetLimitOutput;
import dio.budgeting.application.output.DashboardOutput;
import dio.budgeting.application.output.GoalOutput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Balance;
import dio.budgeting.domain.BudgetLimitRepository;
import dio.budgeting.domain.Expense;
import dio.budgeting.domain.GoalRepository;
import dio.budgeting.domain.Income;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class ListDashboardUseCase {
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TransactionRepository transactionRepository;
    private final GoalRepository goalRepository;
    private final BudgetLimitRepository budgetLimitRepository;

    public ListDashboardUseCase(AuthenticatedUserProvider authenticatedUserProvider,
            TransactionRepository transactionRepository, GoalRepository goalRepository,
            BudgetLimitRepository budgetLimitRepository) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.transactionRepository = transactionRepository;
        this.goalRepository = goalRepository;
        this.budgetLimitRepository = budgetLimitRepository;
    }

    @Tool(name = "list-dashboard-user" ,description = "Lista o estatísticas financeiras do usuário")
    public DashboardOutput execute(){
        var userId = authenticatedUserProvider.currentUserId();

        Expense expense = transactionRepository.totalExpense(userId);
        Income income = transactionRepository.totalIncome(userId);

        Balance balance = new Balance(income.amount() - expense.amount());

        var goal = goalRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        var budgetLimit = budgetLimitRepository.findByUserId(userId)
        .orElseThrow(() -> new IllegalArgumentException("Budget limit not found"));

       var transactions = transactionRepository.findByUserId(userId).stream().map(TransactionOutput::from).toList();

       return new DashboardOutput(balance, income,expense, BudgetLimitOutput.from(budgetLimit), GoalOutput.from(goal), transactions,"");

    }
}
