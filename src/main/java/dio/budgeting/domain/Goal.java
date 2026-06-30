package dio.budgeting.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Goal {
    private GoalId id;
    private UserId userId;

    private String title;

    private Long targetAmount;

    private Long currentAmount;

    private LocalDate deadline;

    public Goal(UserId userId, String title, Long targetAmount, Long currentAmount, LocalDate deadline) {
        this.id = new GoalId();
        this.userId = userId;
        this.title = title;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
    }

    public void deposit(Long amount){
        this.currentAmount += amount;
    }

    public Long remaining(){
        return targetAmount - currentAmount;
    }

    public Integer percentage(){
        return (int)((currentAmount * 100) / targetAmount);
    }

    public boolean completed(){
        return currentAmount >= targetAmount;
    }
}