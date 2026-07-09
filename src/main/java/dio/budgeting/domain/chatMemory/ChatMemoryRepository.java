package dio.budgeting.domain.chatMemory;

import java.util.List;

import dio.budgeting.domain.user.UserId;

public interface ChatMemoryRepository {
    ChatMemory save(ChatMemory chatMemory);

    List<ChatMemory> findByUserId(UserId userId);
}
