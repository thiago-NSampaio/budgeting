package dio.budgeting.domain.audio;

import java.util.UUID;

public record AudioFileId(UUID uuid) {
    public AudioFileId() {
        this(UUID.randomUUID());
    }
}