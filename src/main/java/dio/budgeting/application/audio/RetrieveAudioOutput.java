package dio.budgeting.application.audio;

import java.time.LocalDateTime;
import dio.budgeting.domain.audio.AudioFile;

public record RetrieveAudioOutput(
    String id,
    String interactionId,
    byte[] audioData,
    String fileName,
    String contentType,
    Long size,
    LocalDateTime createdAt
) {
    public static RetrieveAudioOutput from(AudioFile audioFile) {
        return new RetrieveAudioOutput(
            audioFile.getId().uuid().toString(),
            audioFile.getInteractionId(),
            audioFile.getAudioData(),
            audioFile.getFileName(),
            audioFile.getContentType(),
            audioFile.getSize(),
            audioFile.getCreatedAt()
        );
    }
}
