package dio.budgeting.domain;

import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findByCategoryAndUserId(Category category, UserId userId);
    List<Transaction> findByUserId(UserId userId);
    Income totalIncome(UserId userId);
    Expense totalExpense(UserId userId);
}
