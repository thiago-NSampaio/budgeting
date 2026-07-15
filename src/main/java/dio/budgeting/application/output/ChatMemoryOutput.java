package dio.budgeting.application.output;

import dio.budgeting.domain.chatMemory.*;
import dio.budgeting.domain.chatMemory.Error;

import java.time.LocalDateTime;
import java.util.List;

public record ChatMemoryOutput(
    String id,
    String message,
    String response,
    List<ActionTaken> actions,
    Error error,
    RequestConfirmation confirmation,
    Clarification clarification,
    String status,
    Metadata metadata,
    LocalDateTime createdAt
) {
    public static ChatMemoryOutput from(ChatMemory chatMemory) {
        return new ChatMemoryOutput(
            chatMemory.getId().uuid().toString(), 
            chatMemory.getUserMessage(),
            chatMemory.getAssistantText(),
            chatMemory.getActions(),
            chatMemory.getError(),
            chatMemory.getConfirmation(),
            chatMemory.getClarification(),
            chatMemory.getStatus(),
            chatMemory.getMetadata(),
            chatMemory.getCreatedAt()
        );
    }
}