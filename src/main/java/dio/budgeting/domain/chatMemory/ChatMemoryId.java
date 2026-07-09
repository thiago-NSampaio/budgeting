package dio.budgeting.domain.chatMemory;

import java.util.UUID;

public record ChatMemoryId(UUID uuid) {
    public ChatMemoryId(){
        this(UUID.randomUUID()); 
    }
}
