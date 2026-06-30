package dio.budgeting.infrastructure.http.response;

import java.time.LocalDate;

import dio.budgeting.application.output.GoalOutput;

public record GoalResponse(String id, String title, Long targetAmount, Long currentAmount, LocalDate deadline) {
    public static GoalResponse from(GoalOutput output) {
        return new GoalResponse(output.id(), output.title(), output.targetAmount(), output.currentAmount(),
                output.deadline());
    }
}