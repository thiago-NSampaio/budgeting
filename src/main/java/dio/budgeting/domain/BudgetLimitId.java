package dio.budgeting.domain;

import java.util.UUID;

public record BudgetLimitId(UUID uuid) {
      public BudgetLimitId(){
        this(UUID.randomUUID());
    }
}
