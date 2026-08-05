package dio.budgeting.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import dio.budgeting.domain.audio.AudioFile;
import dio.budgeting.domain.audio.AudioFileRepository;
import dio.budgeting.domain.user.UserId;
import dio.budgeting.infrastructure.persistence.entity.AudioFileEntity;

@Repository
public class AudioFileRepositoryAdapter implements AudioFileRepository {
    private final AudioFileJpaRepository jpaRepository;

    public AudioFileRepositoryAdapter(AudioFileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AudioFile save(AudioFile audioFile) {
        AudioFileEntity entity = AudioFileEntity.from(audioFile);
        AudioFileEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<AudioFile> findByInteractionId(String interactionId) {
        return jpaRepository.findByInteractionId(interactionId)
            .map(AudioFileEntity::toDomain);
    }

    @Override
    public List<AudioFile> findByUserId(UserId userId) {
        return jpaRepository.findByUserIdOrderByCreatedAtDesc(userId.uuid())
            .stream()
            .map(AudioFileEntity::toDomain)
            .toList();
    }

    @Override
    public void deleteByInteractionId(String interactionId) {
        jpaRepository.deleteByInteractionId(interactionId);
    }
}
