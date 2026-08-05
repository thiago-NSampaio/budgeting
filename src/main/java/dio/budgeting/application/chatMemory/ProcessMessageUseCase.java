package dio.budgeting.application.chatMemory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import dio.budgeting.domain.chatMemory.AgentResult;
import dio.budgeting.domain.chatMemory.ChatMemory;
import dio.budgeting.domain.chatMemory.ChatMemoryRepository;
import dio.budgeting.domain.dto.assistant.AssistantResponse;
import dio.budgeting.providers.AuthenticatedUserProvider;
import dio.budgeting.service.AudioStorageService;
import dio.budgeting.service.PiperTtsService;
import dio.budgeting.service.TranscriptionService;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProcessMessageUseCase {
    private ChatMemoryRepository chatMemoryRepository;
    private ChatClient chatClient;
    private TranscriptionService whisperClient;
    private PiperTtsService piperClient;
    private AudioStorageService audioStorageService;
    private AuthenticatedUserProvider authenticatedUserProvider;

    public ProcessMessageUseCase(ChatMemoryRepository chatMemoryRepository,
            TranscriptionService whisperClient, PiperTtsService piperClient, AudioStorageService audioStorageService,

            ChatClient.Builder chatClientBuilder, AuthenticatedUserProvider authenticatedUserProvider) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatClient = chatClientBuilder.build();
        this.whisperClient = whisperClient;
        this.piperClient = piperClient;
        this.audioStorageService = audioStorageService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    public AssistantResponse execute(MultipartFile audio, String text) {
        long start = System.currentTimeMillis();
        String interactionId = UUID.randomUUID().toString();
        String userMessage = null;

        try {
            if (audio != null && !audio.isEmpty()) {
                userMessage = whisperClient.transcribeAudio(audio);
            } else if (text != null && !text.isBlank()) {
                userMessage = text;
            } else {
                return buildErrorResponse(interactionId, start, null,
                        "No input provided", "Please provide text or audio.");
            }
        } catch (Exception e) {
            log.error("Whisper transcription failed", e);
            return buildErrorResponse(interactionId, start, null,
                    "STT_ERROR", "I couldn't understand the audio. Please try again.");
        }

        var userId = authenticatedUserProvider.currentUserId();

        List<ChatMemory> recentMemories = chatMemoryRepository
                .findByUserId(userId);
        Collections.reverse(recentMemories);
        if (recentMemories.size() > 20) {
            recentMemories = recentMemories.subList(recentMemories.size() - 20, recentMemories.size());
        }

        List<Message> historyMessages = new ArrayList<>();
        for (ChatMemory mem : recentMemories) {
            historyMessages.add(new UserMessage(mem.getUserMessage()));
            if (mem.getAssistantText() != null) {
                historyMessages.add(new AssistantMessage(mem.getAssistantText()));
            }
        }

        AgentResult agentResult;
        try {

            // Chamar o ChatClient com a fluent API
            String assistantText = chatClient.prompt()
                    .system("You are a financial assistant. Use tools when necessary.")
                    .messages(historyMessages)
                    .user(userMessage)
                    .call()
                    .content();

            // Construir AgentResult
            agentResult = AgentResult.builder()
                    .assistantText(assistantText)
                    .actionsTaken(List.of())
                    .toolsCalled(List.of())
                    .status("OK")
                    .build();
        } catch (Exception e) {
            log.error("LLM processing failed", e);
            return buildErrorResponse(interactionId, start, userMessage,
                    "LLM_ERROR", "Sorry, I had trouble processing your message. Please try again.");
        }

        String audioUrl = null;
        if (agentResult.getAssistantText() != null && !agentResult.getAssistantText().isBlank()) {
            try {
                byte[] audioBytes = piperClient.generateSpeech(agentResult.getAssistantText());
                var storedAudio = audioStorageService.store(interactionId, audioBytes, userId);
                audioUrl = storedAudio != null ? storedAudio.getFileName() : null;
            } catch (Exception e) {
                log.warn("Audio synthesis/storage failed, continuing without audio", e);
            }
        }

        AssistantResponse response = new AssistantResponse(
                userMessage,
                agentResult.getAssistantText(),
                audioUrl,
                agentResult.getActionsTaken(),
                agentResult.getError(),
                agentResult.getConfirmation(),
                agentResult.getClarification(),
                agentResult.getStatus(),
                new AssistantResponse.Metadata(
                        interactionId,
                        Instant.now(),
                        agentResult.getToolsCalled(),
                        System.currentTimeMillis() - start));

        try {

        } catch (Exception e) {
            log.error("Failed to persist chat memory, but response is returned", e);
        }

        return response;
    }

    private AssistantResponse buildErrorResponse(String interactionId, long start, String userMessage,
            String errorCode, String message) {
        return new AssistantResponse(
                userMessage,
                message,
                null,
                null,
                new dio.budgeting.domain.chatMemory.Error(errorCode, message),
                null,
                null,
                "error",
                new AssistantResponse.Metadata(
                        interactionId,
                        Instant.now(),
                        List.of(),
                        System.currentTimeMillis() - start));
    }
}
