package dio.budgeting.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Table;
import jakarta.persistence.Id;

import dio.budgeting.domain.chatMemory.ActionTaken;
import dio.budgeting.domain.chatMemory.ChatMemory;
import dio.budgeting.domain.chatMemory.Clarification;
import dio.budgeting.domain.chatMemory.RequestConfirmation;
import dio.budgeting.domain.chatMemory.Error;
import dio.budgeting.domain.chatMemory.Metadata;
import dio.budgeting.domain.user.UserId;
import dio.budgeting.infrastructure.persistence.converter.ActionsConverter;
import dio.budgeting.infrastructure.persistence.converter.ClarificationConverter;
import dio.budgeting.infrastructure.persistence.converter.ConfirmationConverter;
import dio.budgeting.infrastructure.persistence.converter.ErrorConverter;
import dio.budgeting.infrastructure.persistence.converter.MetadataConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "chat_memories", indexes = {
    @Index(name = "idx_chat_memory_user_id", columnList = "user_id"),
    @Index(name = "idx_chat_memory_created_at", columnList = "created_at")
})
public class ChatMemoryEntity {
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_message", nullable = false, columnDefinition = "TEXT")
    private String userMessage;

    @Column(name = "assistant_text", columnDefinition = "TEXT")
    private String assistantText;

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "actions", columnDefinition = "jsonb")
    @Convert(converter = ActionsConverter.class)
    private List<ActionTaken> actions;

    @Column(name = "error", columnDefinition = "jsonb")
    @Convert(converter = ErrorConverter.class)
    private Error error;

    @Column(name = "confirmation", columnDefinition = "jsonb")
    @Convert(converter = ConfirmationConverter.class)
    private RequestConfirmation confirmation;

    @Column(name = "clarification", columnDefinition = "jsonb")
    @Convert(converter = ClarificationConverter.class)
    private Clarification clarification;

    @Column(name = "metadata", columnDefinition = "jsonb")
    @Convert(converter = MetadataConverter.class)
    private Metadata metadata;

    @Column(name = "status", length = 30)
    private String status;

    public static ChatMemoryEntity from(ChatMemory chatMemory) {
        ChatMemoryEntity entity = new ChatMemoryEntity();
        entity.setId(chatMemory.getId().uuid());
        entity.setUserMessage(chatMemory.getUserMessage());
        entity.setAssistantText(chatMemory.getAssistantText());
        entity.setUserId(chatMemory.getUserId().uuid());
        entity.setCreatedAt(chatMemory.getCreatedAt());
        entity.setActions(chatMemory.getActions());
        entity.setError(chatMemory.getError());
        entity.setConfirmation(chatMemory.getConfirmation());
        entity.setClarification(chatMemory.getClarification());
        entity.setStatus(chatMemory.getStatus());
        return entity;
    }

    public ChatMemory toDomain() {
        return new ChatMemory(
                this.userMessage,
                this.assistantText,
                new UserId(this.userId),
                this.actions,
                this.error,
                this.confirmation,
                this.clarification,
                this.status,
                this.metadata);
    }
}