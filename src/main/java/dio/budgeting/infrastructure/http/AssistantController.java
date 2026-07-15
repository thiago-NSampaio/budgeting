package dio.budgeting.infrastructure.http;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import dio.budgeting.application.chatMemory.ProcessMessageUseCase;
import dio.budgeting.domain.chatMemory.Error;
import dio.budgeting.domain.dto.assistant.AssistantResponse;
import dio.budgeting.providers.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/assistant")
@Slf4j
public class AssistantController {

    private ProcessMessageUseCase processMessageUseCase;

    public AssistantController(ProcessMessageUseCase processMessageUseCase,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.processMessageUseCase = processMessageUseCase;
    }

    @PostMapping(value = "/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssistantResponse> receiveMessage(
        @RequestParam(value = "audio", required = false) MultipartFile audio,
        @RequestParam(value = "text", required = false) String text,
        @AuthenticationPrincipal UserDetails userDetails) {

        // Extract userId from authentication (adjust to your auth mechanism)
        if (userDetails == null) {
            // In real app, authentication filter handles this, but we return a 401-like response
            return ResponseEntity.status(401).body(
                new AssistantResponse(null, "Authentication required.", null, null,
                    new Error("AUTH_ERROR", "Not authenticated"), null, null, "error", null)
            );
        }

        AssistantResponse response = processMessageUseCase.execute(audio, text);
        return ResponseEntity.ok(response);
    }
}
