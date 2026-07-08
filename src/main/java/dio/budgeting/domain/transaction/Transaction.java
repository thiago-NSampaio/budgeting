package dio.budgeting.domain.transaction;

import java.time.LocalDateTime;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.user.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Transaction {
    private TransactionId id;
    private String description;
    private Long amount;
    private Category category;
    private UserId userId;
    private LocalDateTime createdAt;
    private TransactionType type;


    public Transaction(String description, Long amount, Category category, UserId userId, TransactionType type){
        this.id = new TransactionId();
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.userId = userId;
        this.type = type;
    }
}