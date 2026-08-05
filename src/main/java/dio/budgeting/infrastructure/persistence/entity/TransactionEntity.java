package dio.budgeting.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.transaction.Transaction;
import dio.budgeting.domain.transaction.TransactionId;
import dio.budgeting.domain.transaction.TransactionType;
import dio.budgeting.domain.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_user_id", columnList = "user_id")
})
public class TransactionEntity {
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;
    private String description;
    private Long amount;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    public static TransactionEntity from(Transaction transaction) {
        return new TransactionEntity(
            transaction.getId().uuid(),
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getCategory(),
            transaction.getUserId().uuid(),
            transaction.getCreatedAt(),
            transaction.getType()
        );
    }

    public Transaction toDomain() {
        return new Transaction(
            new TransactionId(this.id),
            this.description,
            this.amount,
            this.category,
            new UserId(this.userId),
            this.createdAt,
            this.type
        );
    }
}
