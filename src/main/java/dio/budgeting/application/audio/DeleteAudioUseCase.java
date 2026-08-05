package dio.budgeting.application.audio;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import dio.budgeting.domain.audio.AudioFileRepository;
import dio.budgeting.exception.AudioNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DeleteAudioUseCase {
    private final AudioFileRepository audioFileRepository;

    public DeleteAudioUseCase(AudioFileRepository audioFileRepository) {
        this.audioFileRepository = audioFileRepository;
    }

    @Tool(name = "delete-audio", description = "Remove um arquivo de áudio pelo interactionId")
    public void execute(String interactionId) {
        audioFileRepository.findByInteractionId(interactionId)
            .ifPresentOrElse(
                audio -> {
                    audioFileRepository.deleteByInteractionId(interactionId);
                    log.info("Audio deleted: interactionId={}", interactionId);
                },
                () -> {
                    throw new AudioNotFoundException(
                        "Audio not found for interaction: " + interactionId
                    );
                }
            );
    }
}
