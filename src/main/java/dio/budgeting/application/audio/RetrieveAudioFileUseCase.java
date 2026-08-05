package dio.budgeting.application.audio;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import dio.budgeting.application.output.AudioFileOutput;
import dio.budgeting.domain.audio.AudioFileRepository;
import dio.budgeting.exception.AudioNotFoundException;

@Service
@Slf4j
public class RetrieveAudioFileUseCase {
    private final AudioFileRepository audioFileRepository;

    public RetrieveAudioFileUseCase(AudioFileRepository audioFileRepository) {
        this.audioFileRepository = audioFileRepository;
    }

    public AudioFileOutput execute(String interactionId) {
        var audioFile = audioFileRepository.findByInteractionId(interactionId)
            .orElseThrow(() -> new AudioNotFoundException(
                "Audio not found for interaction: " + interactionId
            ));

        return AudioFileOutput.from(audioFile);
    }
}