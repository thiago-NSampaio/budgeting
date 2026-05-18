package dio.budgeting;

import dio.budgeting.service.PiperTtsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/speech")
public class SpeechController {

    private final PiperTtsService piperTtsService;

    public SpeechController(PiperTtsService piperTtsService) {
        this.piperTtsService = piperTtsService;
    }

    @PostMapping(
            value = "/generate",
            produces = "audio/wav"
    )
    public ResponseEntity<byte[]> generate(
            @RequestBody String text
    ) {

        byte[] audio = piperTtsService.generateSpeech(text);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=speech.wav"
                )
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(audio);
    }
}