package dio.budgeting.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import dio.budgeting.domain.audio.AudioFile;
import dio.budgeting.domain.audio.AudioFileRepository;
import dio.budgeting.domain.user.UserId;
import dio.budgeting.exception.AudioStorageException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DatabaseAudioStorageService implements AudioStorageService {
    private final AudioFileRepository audioFileRepository;

    public DatabaseAudioStorageService(AudioFileRepository audioFileRepository) {
        this.audioFileRepository = audioFileRepository;
    }

    @Override
    public AudioFile store(String interactionId, byte[] audioData, UserId userId) throws AudioStorageException {
        try {
            if (interactionId == null || interactionId.isBlank()) {
                throw new IllegalArgumentException("interactionId must not be null or blank");
            }
            if (audioData == null || audioData.length == 0) {
                throw new IllegalArgumentException("audioData must not be null or empty");
            }
            if (userId == null) {
                throw new IllegalArgumentException("userId must not be null");
            }

            AudioFile audioFile = new AudioFile(interactionId, audioData, userId);
            AudioFile savedFile = audioFileRepository.save(audioFile);
            log.info("Audio file successfully stored for interactionId: {}", interactionId);
            return savedFile;
        } catch (Exception e) {
            log.error("Failed to store audio file for interactionId: {}", interactionId, e);
            throw new AudioStorageException("Failed to store audio file for interactionId: " + interactionId, e);
        }
    }

    @Override
    public Optional<AudioFile> retrieve(String interactionId) {
        if (interactionId == null || interactionId.isBlank()) {
            return Optional.empty();
        }
        return audioFileRepository.findByInteractionId(interactionId);
    }

    @Override
    public void delete(String interactionId) throws AudioStorageException {
        try {
            if (interactionId == null || interactionId.isBlank()) {
                throw new IllegalArgumentException("interactionId must not be null or blank");
            }
            Optional<AudioFile> existing = audioFileRepository.findByInteractionId(interactionId);
            if (existing.isEmpty()) {
                throw new AudioStorageException("Audio file not found for interactionId: " + interactionId);
            }
            audioFileRepository.deleteByInteractionId(interactionId);
            log.info("Audio file successfully deleted for interactionId: {}", interactionId);
        } catch (AudioStorageException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete audio file for interactionId: {}", interactionId, e);
            throw new AudioStorageException("Failed to delete audio file for interactionId: " + interactionId, e);
        }
    }
}
