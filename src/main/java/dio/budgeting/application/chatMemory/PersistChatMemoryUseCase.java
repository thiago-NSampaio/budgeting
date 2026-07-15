package dio.budgeting.application.chatMemory;

import dio.budgeting.application.input.PersistChatMemoryInput;
import dio.budgeting.application.output.ChatMemoryOutput;

import dio.budgeting.domain.chatMemory.ChatMemory;
import dio.budgeting.domain.chatMemory.ChatMemoryRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

public class PersistChatMemoryUseCase {
    private ChatMemoryRepository chatMemoryRepository;
    private AuthenticatedUserProvider authenticatedUserProvider;

    public PersistChatMemoryUseCase(ChatMemoryRepository chatMemoryRepository,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public ChatMemoryOutput execute(PersistChatMemoryInput input) {
        var userId = authenticatedUserProvider.currentUserId();
        var chatMemory = chatMemoryRepository.save(
                new ChatMemory(
                        input.message(),
                        input.response(),
                        userId,
                        input.actions(),
                        input.error(),
                        input.confirmation(),
                        input.clarification(),
                        input.status(),
                        input.metadata()));

        return ChatMemoryOutput.from(chatMemory);
    }
}