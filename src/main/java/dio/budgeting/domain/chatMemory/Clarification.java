package dio.budgeting.domain.chatMemory;

import java.util.List;

public record Clarification(String ask, List<String> options) {}
