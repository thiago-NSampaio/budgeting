package dio.budgeting.domain.transaction;

import java.util.UUID;

public record TransactionId(UUID uuid) {
    public TransactionId(){
        this(UUID.randomUUID());
    }
}
