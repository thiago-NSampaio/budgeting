package dio.budgeting.application.input;

import java.time.LocalDate;

public record PersistGoalInput(String title, Long targetAmount, Long currentAmount, LocalDate deadline) {}


