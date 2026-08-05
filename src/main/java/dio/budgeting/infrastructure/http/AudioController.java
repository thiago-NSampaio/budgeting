package dio.budgeting.infrastructure.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.audio.RetrieveAudioUseCase;
import dio.budgeting.application.audio.RetrieveAudioOutput;

@RestController
@RequestMapping("/api/audio")
public class AudioController {
    private final RetrieveAudioUseCase retrieveAudioUseCase;

    public AudioController(RetrieveAudioUseCase retrieveAudioUseCase) {
        this.retrieveAudioUseCase = retrieveAudioUseCase;
    }

    @GetMapping("/{interactionId}")
    public ResponseEntity<byte[]> getAudio(@PathVariable String interactionId) {
        RetrieveAudioOutput output = retrieveAudioUseCase.execute(interactionId);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(output.contentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + output.fileName() + "\"")
            .body(output.audioData());
    }
}
