package dio.budgeting.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import dio.budgeting.infrastructure.persistence.entity.AudioFileEntity;

public interface AudioFileJpaRepository extends JpaRepository<AudioFileEntity, UUID> {
    Optional<AudioFileEntity> findByInteractionId(String interactionId);
    List<AudioFileEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    void deleteByInteractionId(String interactionId);
}
