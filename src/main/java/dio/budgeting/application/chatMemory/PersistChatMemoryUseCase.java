package dio.budgeting.application.chatMemory;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import dio.budgeting.application.input.PersistChatMemoryInput;
import dio.budgeting.application.output.ChatMemoryOutput;
import dio.budgeting.domain.chatMemory.ChatMemory;
import dio.budgeting.domain.chatMemory.ChatMemoryRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class PersistChatMemoryUseCase {
    private AuthenticatedUserProvider authenticatedUserProvider;
    private ChatMemoryRepository chatMemoryRepository;

    public PersistChatMemoryUseCase(AuthenticatedUserProvider authenticatedUserProvider,
            ChatMemoryRepository chatMemoryRepository) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.chatMemoryRepository = chatMemoryRepository;
    }
 
    @Tool(name = "persist-chat-memory", description = "Persiste a mensagem do usuário e a resposta do agente")
    public ChatMemoryOutput execute(PersistChatMemoryInput input){
        var userId = authenticatedUserProvider.currentUserId();

        var chatMemory = chatMemoryRepository.save(new ChatMemory(input.message(),input.response(),userId));

        return ChatMemoryOutput.from(chatMemory);
    }
}
