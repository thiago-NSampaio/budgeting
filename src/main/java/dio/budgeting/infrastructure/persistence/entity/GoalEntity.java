package dio.budgeting.infrastructure.persistence.entity;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.annotation.Id;

import dio.budgeting.domain.Goal;
import dio.budgeting.domain.GoalId;
import dio.budgeting.domain.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String title;

    @Column(name = "target_amount", nullable = false)
    private Long targetAmount;

    @Column(name = "current_amount", nullable = false)
    private Long currentAmount;

    private LocalDate deadline;

    public static GoalEntity from(Goal goal) {
        return new GoalEntity(
            goal.getId().uuid(),
            goal.getUserId().uuid(),
            goal.getTitle(),
            goal.getTargetAmount(),
            goal.getCurrentAmount(),
            goal.getDeadline()
        );
    }

    public Goal toDomain() {
        return new Goal(
            new GoalId(this.id),
            new UserId(this.userId),
            this.title,
            this.targetAmount,
            this.currentAmount,
            this.deadline
        );
    }
}
