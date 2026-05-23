package dio.budgeting.infrastructure.http;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import dio.budgeting.domain.Category;
import dio.budgeting.infrastructure.http.request.TransactionRequest;
import dio.budgeting.infrastructure.http.response.TransactionResponse;
import dio.budgeting.service.PiperTtsService;
import dio.budgeting.service.TranscriptionService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final TranscriptionService transcriptionService;
    private final ChatClient chatClient;
    private final PiperTtsService piperTtsService;
    // private final TextNormalizerService textNormalizerService;

    public TransactionController(
        PersistTransactionUseCase persistTransactionUseCase,
        ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
        TranscriptionService transcriptionService,
        ChatClient.Builder chatClientBuilder,
        PiperTtsService piperTtsService,
        @Value("classpath:/prompts/system-message.st") Resource systemPrompt
    ) throws IOException {
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.transcriptionService = transcriptionService;
        this.chatClient = 
        chatClientBuilder.defaultSystem(systemPrompt)
                        .defaultTools(persistTransactionUseCase, listTransactionsByCategoryUseCase).build();
        this.piperTtsService = piperTtsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(@RequestBody TransactionRequest request){
        var transaction = persistTransactionUseCase.execute(request.toInput());
        return TransactionResponse.from(transaction);
    }

    @GetMapping("/{category}")
    public List<TransactionResponse> readTransactions(@PathVariable Category category){
        return listTransactionsByCategoryUseCase.execute(category).stream().map(TransactionResponse::from).toList();
    }

    @PostMapping(value = "/ai", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "audio/mp3")
    ResponseEntity<ByteArrayResource> transcribe(@RequestParam("file") MultipartFile file) {
        var userMessage = transcriptionService.transcribeAudio(file);

        var result = chatClient.prompt().user(userMessage).call().content();

        byte[] audio = piperTtsService.generateSpeech(result);

        var resource = new ByteArrayResource(audio);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("audio.mp3").build().toString()).body(resource);
    }
}
