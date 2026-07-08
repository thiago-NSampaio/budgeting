package dio.budgeting.domain.transaction;

import java.util.List;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Expense;
import dio.budgeting.domain.Income;
import dio.budgeting.domain.user.UserId;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findByCategoryAndUserId(Category category, UserId userId);
    List<Transaction> findByUserId(UserId userId);
    Income totalIncome(UserId userId);
    Expense totalExpense(UserId userId);
}
