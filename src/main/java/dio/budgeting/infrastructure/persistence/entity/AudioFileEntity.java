package dio.budgeting.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import dio.budgeting.domain.audio.AudioFile;
import dio.budgeting.domain.audio.AudioFileId;
import dio.budgeting.domain.user.UserId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audio_files", indexes = {
    @Index(name = "idx_audio_file_interaction_id", columnList = "interaction_id"),
    @Index(name = "idx_audio_file_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AudioFileEntity {
    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "interaction_id", nullable = false)
    private String interactionId;

    @Lob
    @Column(name = "audio_data", columnDefinition = "LONGBLOB")
    private byte[] audioData;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long size;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID userId;

    public static AudioFileEntity from(AudioFile audioFile) {
        return new AudioFileEntity(
            audioFile.getId().uuid(),
            audioFile.getInteractionId(),
            audioFile.getAudioData(),
            audioFile.getFileName(),
            audioFile.getSize(),
            audioFile.getContentType(),
            audioFile.getCreatedAt(),
            audioFile.getUserId().uuid()
        );
    }

    public AudioFile toDomain() {
        return new AudioFile(
            new AudioFileId(this.id),
            this.interactionId,
            this.audioData,
            this.fileName,
            this.size,
            this.contentType,
            this.createdAt,
            new UserId(this.userId)
        );
    }
}