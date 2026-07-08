package dio.budgeting.domain.balance;

import java.util.UUID;

public record BudgetLimitId(UUID uuid) {
      public BudgetLimitId(){
        this(UUID.randomUUID());
    }
}
