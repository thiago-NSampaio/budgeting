package dio.budgeting.domain.audio;

import java.time.LocalDateTime;

import dio.budgeting.domain.user.UserId;

public class AudioFile {
    private final AudioFileId id;
    private final String interactionId;
    private final byte[] audioData;
    private final String fileName;
    private final Long size;
    private final String contentType;
    private final LocalDateTime createdAt;
    private final UserId userId;

    // Constructor with auto-generation
    public AudioFile(String interactionId, byte[] audioData, UserId userId) {
        if (interactionId == null || interactionId.isBlank()) {
            throw new IllegalArgumentException("interactionId is required");
        }
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("audioData is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        this.id = new AudioFileId();
        this.interactionId = interactionId;
        this.audioData = audioData;
        this.fileName = interactionId + ".mp3";
        this.size = (long) audioData.length;
        this.contentType = "audio/mpeg";
        this.createdAt = LocalDateTime.now();
        this.userId = userId;
    }

    // Full constructor for reconstruction from database
    public AudioFile(AudioFileId id, String interactionId, byte[] audioData, 
                     String fileName, Long size, String contentType, 
                     LocalDateTime createdAt, UserId userId) {
        this.id = id;
        this.interactionId = interactionId;
        this.audioData = audioData;
        this.fileName = fileName;
        this.size = size;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public AudioFileId getId() {
        return id;
    }

    public String getInteractionId() {
        return interactionId;
    }

    public byte[] getAudioData() {
        return audioData;
    }

    public String getFileName() {
        return fileName;
    }

    public Long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserId getUserId() {
        return userId;
    }
}