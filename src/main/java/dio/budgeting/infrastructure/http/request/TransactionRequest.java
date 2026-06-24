package dio.budgeting.infrastructure.http.request;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.UserId;

public record TransactionRequest(String description, Category category, Long amount, UserId userId) {
    public PersistTransactionInput toInput(){
        return new PersistTransactionInput(description,amount,category, userId);
    }
}
