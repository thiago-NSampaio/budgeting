package dio.budgeting.infrastructure.http;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.PersistGoalUseCase;

import dio.budgeting.infrastructure.http.request.GoalRequest;
import dio.budgeting.infrastructure.http.response.GoalResponse;

@RestController
@RequestMapping("/goals")
public class GoalController {
    private PersistGoalUseCase persistGoalUseCase;

    public GoalController(PersistGoalUseCase persistGoalUseCase) {
        this.persistGoalUseCase = persistGoalUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(@RequestBody GoalRequest request){
       var goal = this.persistGoalUseCase.execute(request.toInput());
       return GoalResponse.from(goal);
    }
}