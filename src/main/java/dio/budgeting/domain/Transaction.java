package dio.budgeting.domain;

import java.time.LocalDateTime;

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

    public Transaction(String description, Long amount, Category category, UserId userId){
        this.id = new TransactionId();
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.userId = userId;
    }
}
