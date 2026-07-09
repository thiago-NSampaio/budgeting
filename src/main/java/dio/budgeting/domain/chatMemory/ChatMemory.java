package dio.budgeting.domain.chatMemory;

import dio.budgeting.domain.user.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMemory {
    private ChatMemoryId id;
    private String message;
    private String response;
    private UserId userId;

    public ChatMemory(String message,String response, UserId userId) {
        this.id = new ChatMemoryId();
        this.message = message;
        this.response = response;
        this.userId = userId;
    }
}
