package dio.budgeting.application.output;

import dio.budgeting.domain.chatMemory.ChatMemory;

public record ChatMemoryOutput(String id, String message, String response) {
    public static ChatMemoryOutput from(ChatMemory chatMemory) {
        return new ChatMemoryOutput(chatMemory.getId().uuid().toString(), chatMemory.getMessage(), chatMemory.getResponse());
    }
}
