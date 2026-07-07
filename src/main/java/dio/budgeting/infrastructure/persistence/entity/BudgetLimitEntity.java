package dio.budgeting.infrastructure.persistence.entity;

import java.time.YearMonth;
import java.util.UUID;

import dio.budgeting.domain.BudgetLimit;
import dio.budgeting.domain.BudgetLimitId;
import dio.budgeting.domain.UserId;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budget_limits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLimitEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private YearMonth month;

    private Long limitAmount;

    public static BudgetLimitEntity from(BudgetLimit budgetLimit){
        return new BudgetLimitEntity(budgetLimit.getId().uuid(), budgetLimit.getUserId().uuid(), budgetLimit.getMonth(), budgetLimit.getLimitAmount()); 
    }

    public BudgetLimit toDomain(){
        return new BudgetLimit(new BudgetLimitId(this.id),new UserId(this.userId), this.month, this.limitAmount);
    }
}
