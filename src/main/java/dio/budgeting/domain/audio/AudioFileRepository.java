package dio.budgeting.domain.audio;

import dio.budgeting.domain.user.UserId;
import java.util.List;
import java.util.Optional;

public interface AudioFileRepository {
    AudioFile save(AudioFile audioFile);
    Optional<AudioFile> findByInteractionId(String interactionId);
    List<AudioFile> findByUserId(UserId userId);
    void deleteByInteractionId(String interactionId);
}