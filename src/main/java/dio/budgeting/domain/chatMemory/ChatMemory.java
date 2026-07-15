package dio.budgeting.domain.chatMemory;

import java.time.LocalDateTime;
import java.util.List;
import dio.budgeting.domain.user.UserId;
import lombok.Getter;

@Getter
public class ChatMemory {
    private ChatMemoryId id;
    private String userMessage;
    private String assistantText;
    private UserId userId;
    private LocalDateTime createdAt;
    private List<ActionTaken> actions;
    private Error error;
    private RequestConfirmation confirmation;
    private Clarification clarification;
    private String status;
    private Metadata metadata;

    public ChatMemory(String userMessage, String assistantText, UserId userId,
            List<ActionTaken> actions, Error error, RequestConfirmation confirmation, Clarification clarification,
            String status, Metadata metadata) {
        this.id = new ChatMemoryId();
        this.userMessage = userMessage;
        this.assistantText = assistantText;
        this.userId = userId;
        this.actions = actions;
        this.error = error;
        this.confirmation = confirmation;
        this.clarification = clarification;
        this.status = status;
        this.metadata = metadata;
    }
}
