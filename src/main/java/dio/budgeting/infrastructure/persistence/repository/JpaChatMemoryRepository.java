package dio.budgeting.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import dio.budgeting.domain.chatMemory.ChatMemory;
import dio.budgeting.domain.chatMemory.ChatMemoryRepository;
import dio.budgeting.domain.user.UserId;
import dio.budgeting.infrastructure.persistence.entity.ChatMemoryEntity;

@Repository
public class JpaChatMemoryRepository implements ChatMemoryRepository {
    private final ChatMemoryEntityRepository chatMemoryEntityRepository;

    public JpaChatMemoryRepository(ChatMemoryEntityRepository chatMemoryEntityRepository) {
        this.chatMemoryEntityRepository = chatMemoryEntityRepository;
    }

    @Override
    public ChatMemory save(ChatMemory chatMemory) {
        var entity = ChatMemoryEntity.from(chatMemory);
        return chatMemoryEntityRepository.save(entity).toDomain();
    }

    @Override
    public List<ChatMemory> findByUserId(UserId userId) {
        return chatMemoryEntityRepository.findByUserId(userId.uuid()).stream().map(ChatMemoryEntity::toDomain).toList();
    }
}
