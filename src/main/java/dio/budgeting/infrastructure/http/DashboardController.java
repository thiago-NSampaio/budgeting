package dio.budgeting.infrastructure.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.ListBudgetLimitUseCase;
import dio.budgeting.application.ListGoalUseCase;
import dio.budgeting.application.ListTransactionsByUserUseCase;
import dio.budgeting.providers.AuthenticatedUserProvider;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final ListTransactionsByUserUseCase listTransactionsByUserUseCase;
    private final ListGoalUseCase listGoalUseCase;
    private final ListBudgetLimitUseCase listBudgetLimitUseCase;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    
}
