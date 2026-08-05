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
import dio.budgeting.application.dto.assistant.AssistantResponse;
import dio.budgeting.application.input.ProcessMessageInput;
import dio.budgeting.domain.chatMemory.Error;
import dio.budgeting.providers.AuthenticatedUserProvider;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/assistant")
@Slf4j
public class AssistantController {

    private final ProcessMessageUseCase processMessageUseCase;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public AssistantController(ProcessMessageUseCase processMessageUseCase,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.processMessageUseCase = processMessageUseCase;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping(value = "/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssistantResponse> receiveMessage(
        @RequestParam(value = "audio", required = false) MultipartFile audio,
        @RequestParam(value = "text", required = false) String text,
        @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(
                new AssistantResponse(null, "Authentication required.", null, null,
                    new Error("AUTH_ERROR", "Not authenticated"), null, null, "error", null)
            );
        }

        try {
            byte[] audioBytes = (audio != null && !audio.isEmpty()) ? audio.getBytes() : null;
            String userId = authenticatedUserProvider.currentUserId().uuid().toString();
            ProcessMessageInput input = new ProcessMessageInput(audioBytes, text, userId);
            AssistantResponse response = processMessageUseCase.execute(input);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing request in controller", e);
            return ResponseEntity.status(500).body(
                new AssistantResponse(null, "Internal server error.", null, null,
                    new Error("SERVER_ERROR", e.getMessage()), null, null, "error", null)
            );
        }
    }
}
