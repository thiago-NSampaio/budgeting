package dio.budgeting.infrastructure.http;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.budget.ListBudgetLimitUseCase;
import dio.budgeting.application.budget.PersistBudgetLimitUseCase;
import dio.budgeting.infrastructure.http.request.BudgetLimitRequest;
import dio.budgeting.infrastructure.http.response.BudgetLimitResponse;

@RestController
@RequestMapping("/budget-limit")
public class BudgetLimitController {
    private final PersistBudgetLimitUseCase persistBudgetLimitUseCase;
    private final ListBudgetLimitUseCase getBudgetLimitUseCase;

    public BudgetLimitController(PersistBudgetLimitUseCase persistBudgetLimitUseCase,
            ListBudgetLimitUseCase getBudgetLimitUseCase) {
        this.persistBudgetLimitUseCase = persistBudgetLimitUseCase;
        this.getBudgetLimitUseCase = getBudgetLimitUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetLimitResponse create(@RequestBody BudgetLimitRequest request) {
        var budgetLimit = persistBudgetLimitUseCase.execute(request.toInput());

        return BudgetLimitResponse.from(budgetLimit);
    }

    @GetMapping
    public BudgetLimitResponse get() {
        return BudgetLimitResponse.from(getBudgetLimitUseCase.execute());
    }
}
