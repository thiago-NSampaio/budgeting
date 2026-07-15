package dio.budgeting.application.input;

import java.util.List;

import dio.budgeting.domain.chatMemory.ActionTaken;
import dio.budgeting.domain.chatMemory.Clarification;
import dio.budgeting.domain.chatMemory.RequestConfirmation;
import dio.budgeting.domain.chatMemory.Metadata;
import dio.budgeting.domain.chatMemory.Error;

public record PersistChatMemoryInput(
        String message,
        String response,
        List<ActionTaken> actions,
        Error error,
        RequestConfirmation confirmation,
        Clarification clarification,
        String status,
        Metadata metadata) {
}