package dio.budgeting.application.chatMemory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

import dio.budgeting.application.audio.StoreAudioInput;
import dio.budgeting.application.audio.StoreAudioOutput;
import dio.budgeting.application.audio.StoreAudioUseCase;
import dio.budgeting.application.budget.ListBudgetLimitUseCase;
import dio.budgeting.application.dto.assistant.AssistantResponse;
import dio.budgeting.application.goal.ListGoalUseCase;
import dio.budgeting.application.input.ProcessMessageInput;
import dio.budgeting.application.transaction.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.transaction.ListTransactionsByUserUseCase;
import dio.budgeting.application.transaction.PersistTransactionUseCase;
import dio.budgeting.application.ListDashboardUseCase;
import dio.budgeting.domain.ActionType;
import dio.budgeting.domain.chatMemory.ActionTaken;
import dio.budgeting.domain.chatMemory.AgentResult;
import dio.budgeting.domain.chatMemory.ChatMemory;
import dio.budgeting.domain.chatMemory.ChatMemoryRepository;
import dio.budgeting.domain.user.UserId;
import dio.budgeting.providers.AuthenticatedUserProvider;
import dio.budgeting.service.PiperTtsService;
import dio.budgeting.service.TranscriptionService;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ProcessMessageUseCase {
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatClient chatClient;
    private final TranscriptionService whisperClient;
    private final PiperTtsService piperClient;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final StoreAudioUseCase storeAudioUseCase;

    // Tool dependencies
    private final PersistTransactionUseCase persistTransactionUseCase;
    private final ListTransactionsByUserUseCase listTransactionsByUserUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final ListDashboardUseCase listDashboardUseCase;
    private final ListGoalUseCase listGoalUseCase;
    private final ListChatMemoryUseCase listChatMemoryUseCase;
    private final ListBudgetLimitUseCase listBudgetLimitUseCase;

    public ProcessMessageUseCase(
            ChatMemoryRepository chatMemoryRepository,
            TranscriptionService whisperClient,
            PiperTtsService piperClient,
            ChatClient.Builder chatClientBuilder,
            AuthenticatedUserProvider authenticatedUserProvider,
            StoreAudioUseCase storeAudioUseCase,
            PersistTransactionUseCase persistTransactionUseCase,
            ListTransactionsByUserUseCase listTransactionsByUserUseCase,
            ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
            ListDashboardUseCase listDashboardUseCase,
            ListGoalUseCase listGoalUseCase,
            ListChatMemoryUseCase listChatMemoryUseCase,
            ListBudgetLimitUseCase listBudgetLimitUseCase) {
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatClient = chatClientBuilder.build();
        this.whisperClient = whisperClient;
        this.piperClient = piperClient;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.storeAudioUseCase = storeAudioUseCase;
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.listTransactionsByUserUseCase = listTransactionsByUserUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.listDashboardUseCase = listDashboardUseCase;
        this.listGoalUseCase = listGoalUseCase;
        this.listChatMemoryUseCase = listChatMemoryUseCase;
        this.listBudgetLimitUseCase = listBudgetLimitUseCase;
    }

    public AssistantResponse execute(ProcessMessageInput input) {
        long start = System.currentTimeMillis();
        String interactionId = UUID.randomUUID().toString();
        String userMessage = null;

        try {
            if (input.audioBytes() != null && input.audioBytes().length > 0) {
                MultipartFile multipartFile = new ByteArrayMultipartFile(input.audioBytes(), interactionId + ".mp3");
                userMessage = whisperClient.transcribeAudio(multipartFile);
            } else if (input.text() != null && !input.text().isBlank()) {
                userMessage = input.text();
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

        List<ChatMemory> recentMemories = chatMemoryRepository.findByUserId(userId);
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
            var chatResponse = chatClient.prompt()
                    .system("You are a financial assistant. Use tools when necessary.")
                    .messages(historyMessages)
                    .user(userMessage)
                    .tools(persistTransactionUseCase, listGoalUseCase, listBudgetLimitUseCase,
                           listTransactionsByUserUseCase, listTransactionsByCategoryUseCase,
                           listDashboardUseCase, listChatMemoryUseCase)
                    .call()
                    .chatResponse();

            String assistantText = chatResponse.getResult().getOutput().getText();

            List<ActionTaken> actions = new ArrayList<>();
            List<String> toolsCalled = new ArrayList<>();

            if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                var assistantMessage = chatResponse.getResult().getOutput();
                if (assistantMessage.getToolCalls() != null) {
                    for (var toolCall : assistantMessage.getToolCalls()) {
                        String toolName = toolCall.name();
                        toolsCalled.add(toolName);

                        switch (toolName) {
                            case "persist-transaction":
                                actions.add(new ActionTaken(ActionType.TRANSACTION_CREATED, "Persisted transaction", toolCall.arguments()));
                                break;
                            case "list-financial-goal":
                                actions.add(new ActionTaken(ActionType.GOAL_LISTED, "Listed goals", toolCall.arguments()));
                                break;
                            case "create-financial-goal":
                            case "createGoal":
                                actions.add(new ActionTaken(ActionType.GOAL_UPDATED, "Created goal", toolCall.arguments()));
                                break;
                            default:
                                actions.add(new ActionTaken(ActionType.QUERY_EXECUTED, "Executed tool: " + toolName, toolCall.arguments()));
                                break;
                        }
                    }
                }
            }

            agentResult = AgentResult.builder()
                    .assistantText(assistantText)
                    .actionsTaken(actions)
                    .toolsCalled(toolsCalled)
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
                StoreAudioInput audioInput = new StoreAudioInput(interactionId, audioBytes);
                StoreAudioOutput audioOutput = storeAudioUseCase.execute(audioInput);
                audioUrl = audioOutput.audioUrl();
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
            ChatMemory memory = new ChatMemory(
                userMessage,
                agentResult.getAssistantText(),
                userId,
                agentResult.getActionsTaken() != null ? agentResult.getActionsTaken() : List.of(),
                agentResult.getError(),
                agentResult.getConfirmation(),
                agentResult.getClarification(),
                agentResult.getStatus() != null ? agentResult.getStatus() : "OK",
                new dio.budgeting.domain.chatMemory.Metadata(
                    interactionId,
                    Instant.now(),
                    agentResult.getToolsCalled() != null ? agentResult.getToolsCalled() : List.of(),
                    System.currentTimeMillis() - start
                )
            );
            chatMemoryRepository.save(memory);
            log.info("Chat memory persisted: id={}", memory.getId().uuid());
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

    private static class ByteArrayMultipartFile implements MultipartFile {
        private final byte[] bytes;
        private final String name;

        public ByteArrayMultipartFile(byte[] bytes, String name) {
            this.bytes = bytes;
            this.name = name;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return name; }
        @Override public String getContentType() { return "audio/mpeg"; }
        @Override public boolean isEmpty() { return bytes == null || bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() throws IOException { return bytes; }
        @Override public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(bytes); }
        @Override public void transferTo(File dest) throws IOException, IllegalStateException {
            Files.write(dest.toPath(), bytes);
        }
    }
}
