package dio.budgeting.service;

import java.util.Optional;

import dio.budgeting.domain.audio.AudioFile;
import dio.budgeting.domain.user.UserId;
import dio.budgeting.exception.AudioStorageException;

public interface AudioStorageService {
    AudioFile store(String interactionId, byte[] audioData, UserId userId) throws AudioStorageException;
    Optional<AudioFile> retrieve(String interactionId);
    void delete(String interactionId) throws AudioStorageException;
}
