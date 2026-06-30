package dio.budgeting.infrastructure.http.request;

import java.time.LocalDate;

import dio.budgeting.application.input.PersistGoalInput;

public record GoalRequest(String title, Long targetAmount, Long currentAmount, LocalDate deadline) {
    public PersistGoalInput toInput(){
        return new PersistGoalInput(title,targetAmount,currentAmount,deadline);
    }
}
