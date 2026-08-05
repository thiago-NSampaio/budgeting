package dio.budgeting.application.audio;

import java.time.LocalDateTime;
import dio.budgeting.domain.audio.AudioFile;

public record StoreAudioOutput(
    String id,
    String audioUrl,
    String interactionId,
    String fileName,
    Long size,
    String contentType,
    LocalDateTime createdAt
) {
    public static StoreAudioOutput from(AudioFile audioFile) {
        return new StoreAudioOutput(
            audioFile.getId().uuid().toString(),
            "/api/audio/" + audioFile.getInteractionId(),
            audioFile.getInteractionId(),
            audioFile.getFileName(),
            audioFile.getSize(),
            audioFile.getContentType(),
            audioFile.getCreatedAt()
        );
    }
}
