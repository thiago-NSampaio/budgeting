package dio.budgeting.infrastructure.http;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.PersistBudgetLimitUseCase;
import dio.budgeting.infrastructure.http.request.BudgetLimitRequest;
import dio.budgeting.infrastructure.http.response.BudgetLimitResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/budget-limit")

public class BudgetLimitController {
    private PersistBudgetLimitUseCase persistBudgetLimitUseCase;
    
    public BudgetLimitController(PersistBudgetLimitUseCase persistBudgetLimitUseCase) {
        this.persistBudgetLimitUseCase = persistBudgetLimitUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetLimitResponse create(@RequestBody BudgetLimitRequest request) {
        var budgetLimit = persistBudgetLimitUseCase.execute(request.toInput());

        return BudgetLimitResponse.from(budgetLimit);
    }
}
