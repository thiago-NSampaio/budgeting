package dio.budgeting.application.audio;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import dio.budgeting.domain.audio.AudioFile;
import dio.budgeting.domain.audio.AudioFileRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StoreAudioUseCase {
    private final AudioFileRepository audioFileRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public StoreAudioUseCase(AudioFileRepository audioFileRepository,
                              AuthenticatedUserProvider authenticatedUserProvider) {
        this.audioFileRepository = audioFileRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Tool(name = "store-audio", description = "Armazena um arquivo de áudio MP3 gerado pelo assistente")
    public StoreAudioOutput execute(StoreAudioInput input) {
        var userId = authenticatedUserProvider.currentUserId();

        var audioFile = audioFileRepository.save(
            new AudioFile(input.interactionId(), input.audioData(), userId)
        );

        log.info("Audio stored successfully: interactionId={}", input.interactionId());
        return StoreAudioOutput.from(audioFile);
    }
}
