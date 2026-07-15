package dio.budgeting.application.chatMemory;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import dio.budgeting.application.output.ChatMemoryOutput;
import dio.budgeting.domain.chatMemory.ChatMemoryRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class ListChatMemoryUseCase {
    private ChatMemoryRepository chatMemoryRepository;
    private AuthenticatedUserProvider authenticatedUserProvider;

    public ListChatMemoryUseCase(ChatMemoryRepository chatMemoryRepository,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Tool(name = "list-chat-memory", description = "Lista o histórico de chat do usuário")
    public List<ChatMemoryOutput> execute() {
        var userId = authenticatedUserProvider.currentUserId();

        return chatMemoryRepository.findByUserId(userId).stream().map(ChatMemoryOutput::from).toList();
    }
}
