package dio.budgeting.application.output;

import java.time.LocalDate;

import dio.budgeting.domain.goal.Goal;

public record GoalOutput(String id, String title, Long targetAmount, Long currentAmount, LocalDate deadline) {
    public static GoalOutput from(Goal goal){
        return new GoalOutput(
            goal.getId().uuid().toString(),
            goal.getTitle(),
            goal.getTargetAmount(),
            goal.getCurrentAmount(),
            goal.getDeadline()
        );
    }
}
