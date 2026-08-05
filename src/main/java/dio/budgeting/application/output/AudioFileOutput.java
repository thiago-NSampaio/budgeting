package dio.budgeting.application.output;

import java.time.LocalDateTime;
import dio.budgeting.domain.audio.AudioFile;

public record AudioFileOutput(
    String audioUrl,
    String interactionId,
    String fileName,
    Long size,
    String contentType,
    LocalDateTime createdAt
) {
    public static AudioFileOutput from(AudioFile audioFile) {
        return new AudioFileOutput(
            "/api/audio/" + audioFile.getInteractionId(),
            audioFile.getInteractionId(),
            audioFile.getFileName(),
            audioFile.getSize(),
            audioFile.getContentType(),
            audioFile.getCreatedAt()
        );
    }
}