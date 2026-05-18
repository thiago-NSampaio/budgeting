package dio.budgeting;

import dio.budgeting.domain.dto.TranscriptionResponse;
import dio.budgeting.service.TranscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller REST para operações de transcrição de áudio.
 * Implementa arquitetura limpa com separação de responsabilidades.
 */
@RestController
@RequestMapping("/transcription")
public class TranscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(TranscriptionController.class);

    private final TranscriptionService transcriptionService;

    /**
     * Construtor com injeção de dependências via construtor.
     *
     * @param transcriptionService serviço de transcrição
     */
    public TranscriptionController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    /**
     * Endpoint POST para transcrição de áudio.
     *
     * @param audio arquivo de áudio em formato multipart/form-data
     * @return ResponseEntity com a transcrição em JSON
     * @apiNote Content-Type: multipart/form-data
     * @apiNote Suporta formatos: mp3, wav, m4a, flac, ogg (conforme suporte do Whisper)
     * @apiNote Timeout: 5 minutos
     */
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TranscriptionResponse> transcribeAudio(
            @RequestParam("audio") MultipartFile audio
    ) {
        logger.info("Recebido requisição de transcrição. Arquivo: {}, Tamanho: {} bytes",
                audio.getOriginalFilename(), audio.getSize());

        String transcription = transcriptionService.transcribeAudio(audio);
        TranscriptionResponse response = new TranscriptionResponse(transcription);

        logger.info("Resposta de transcrição enviada com sucesso");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
