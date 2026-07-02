package dio.budgeting.infrastructure.http;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.listGoalUseCase;
import dio.budgeting.application.PersistGoalUseCase;
import dio.budgeting.infrastructure.http.request.GoalRequest;
import dio.budgeting.infrastructure.http.response.GoalResponse;

@RestController
@RequestMapping("/goals")
public class GoalController {
    private final PersistGoalUseCase persistGoalUseCase;
    private final listGoalUseCase getGoalUseCase;

    public GoalController(PersistGoalUseCase persistGoalUseCase, listGoalUseCase getGoalUseCase) {
        this.persistGoalUseCase = persistGoalUseCase;
        this.getGoalUseCase = getGoalUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(@RequestBody GoalRequest request){
       var goal = this.persistGoalUseCase.execute(request.toInput());
       return GoalResponse.from(goal);
    }

    @GetMapping
    public GoalResponse get() {
        return GoalResponse.from(getGoalUseCase.execute());
    }
}