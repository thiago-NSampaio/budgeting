package dio.budgeting.application.dto.assistant;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import dio.budgeting.domain.chatMemory.ActionTaken;
import dio.budgeting.domain.chatMemory.Clarification;
import dio.budgeting.domain.chatMemory.Error;
import dio.budgeting.domain.chatMemory.RequestConfirmation;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssistantResponse(
    String userMessage,
    String assistantMessage,
    String audioUrl,
    List<ActionTaken> actions,
    Error error,
    RequestConfirmation confirmation,
    Clarification clarification,
    String status,
    Metadata metadata
) {
    public record Metadata(
        String interactionId,
        Instant timestamp,
        List<String> toolsUsed,
        long durationMs
    ) {}
}
