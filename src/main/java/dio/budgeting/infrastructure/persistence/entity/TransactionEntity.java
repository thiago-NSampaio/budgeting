package dio.budgeting.infrastructure.persistence.entity;

import java.util.UUID;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionId;
import dio.budgeting.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {
    @Id
    private UUID id;
    private String description;
    private Long amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    public static TransactionEntity from(Transaction transaction) {
        return new TransactionEntity(
            transaction.getId().uuid(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getCategory(),
            transaction.getUserId().uuid()
        );
    }

    public Transaction toDomain() {
        return new Transaction(
            new TransactionId(this.id),
            this.description,
            this.amount,
            this.category,
            new UserId(this.userId)
        );
    }
}
