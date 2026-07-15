package dio.budgeting.service;

import java.util.List;

import org.springframework.ai.chat.messages.Message;

import dio.budgeting.domain.chatMemory.AgentResult;

public interface ChatService {
    AgentResult process(String userMessage, List<Message> history);
}
