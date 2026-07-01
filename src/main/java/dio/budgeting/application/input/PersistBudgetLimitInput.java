package dio.budgeting.application.input;

import java.time.YearMonth;

public record PersistBudgetLimitInput(YearMonth month, Long limitAmount) {
    
}
