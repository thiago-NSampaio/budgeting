package dio.budgeting.application.chatMemory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import dio.budgeting.service.ChatService;
import dio.budgeting.service.PiperClient;
import dio.budgeting.service.WhisperClient;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProcessMessageUseCase {
    private ChatMemoryRepository chatMemoryRepository;
    private ChatService chatService;
    private WhisperClient whisperClient;
    private PiperClient piperClient;
    private AudioStorageService audioStorageService;
    private AuthenticatedUserProvider authenticatedUserProvider;

    public ProcessMessageUseCase(ChatMemoryRepository chatMemoryRepository, ChatService chatService,
            WhisperClient whisperClient, PiperClient piperClient, AudioStorageService audioStorageService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatService = chatService;
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
                userMessage = whisperClient.transcribe(audio.getInputStream());
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
            agentResult = chatService.process(userMessage, historyMessages);
        } catch (Exception e) {
            log.error("LLM processing failed", e);
            return buildErrorResponse(interactionId, start, userMessage,
                "LLM_ERROR", "Sorry, I had trouble processing your message. Please try again.");
        }

        String audioUrl = null;
        if (agentResult.getAssistantText() != null && !agentResult.getAssistantText().isBlank()) {
            try {
                byte[] audioBytes = piperClient.synthesize(agentResult.getAssistantText());
                audioUrl = audioStorageService.store(interactionId, audioBytes);
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
                System.currentTimeMillis() - start
            )
        );

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
                System.currentTimeMillis() - start
            )
        );
    }
}
