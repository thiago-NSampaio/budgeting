package dio.budgeting.domain.balance;

import java.time.YearMonth;

import dio.budgeting.domain.user.UserId;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BudgetLimit {
    private BudgetLimitId id;

    @Column(name = "user_id")
    private UserId userId;

    private YearMonth month;

    private Long limitAmount;

    public BudgetLimit(UserId userId, YearMonth month, Long limitAmount) {
        this.id = new BudgetLimitId();
        this.userId = userId;
        this.month = month;
        this.limitAmount = limitAmount;
    }
}
