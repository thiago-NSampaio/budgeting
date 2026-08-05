package dio.budgeting.application.audio;

import org.springframework.stereotype.Service;

import dio.budgeting.domain.audio.AudioFileRepository;
import dio.budgeting.exception.AudioNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RetrieveAudioUseCase {
    private final AudioFileRepository audioFileRepository;

    public RetrieveAudioUseCase(AudioFileRepository audioFileRepository) {
        this.audioFileRepository = audioFileRepository;
    }

    public RetrieveAudioOutput execute(String interactionId) {
        var audioFile = audioFileRepository.findByInteractionId(interactionId)
            .orElseThrow(() -> new AudioNotFoundException(
                "Audio not found for interaction: " + interactionId
            ));

        return RetrieveAudioOutput.from(audioFile);
    }
}
