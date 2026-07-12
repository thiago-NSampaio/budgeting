package dio.budgeting.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import dio.budgeting.domain.chatMemory.ChatMemory;
import dio.budgeting.domain.chatMemory.ChatMemoryId;

import dio.budgeting.domain.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "chat-memorys")
public class ChatMemoryEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String response;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static ChatMemoryEntity from(ChatMemory chatMemory) {
        return new ChatMemoryEntity(
                chatMemory.getId().uuid(),
                chatMemory.getResponse(),
                chatMemory.getMessage(),
                chatMemory.getUserId().uuid(),
                chatMemory.getCreatedAt());
    }

    public ChatMemory toDomain() {
        return new ChatMemory(
                new ChatMemoryId(this.id),
                this.message,
                this.response,
                new UserId(this.userId),
                this.createdAt);
    }
}
