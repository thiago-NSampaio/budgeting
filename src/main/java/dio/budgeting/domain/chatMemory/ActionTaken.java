package dio.budgeting.domain.chatMemory;

import dio.budgeting.domain.ActionType;

public record ActionTaken(ActionType type, String description, Object data) {}
