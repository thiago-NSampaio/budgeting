package dio.budgeting.infrastructure.http;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.ListBudgetLimitUseCase;
import dio.budgeting.application.ListGoalUseCase;
import dio.budgeting.application.ListTransactionsByUserUseCase;
import dio.budgeting.providers.AuthenticatedUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final ListTransactionsByUserUseCase listTransactionsByUserUseCase;
    private final ListGoalUseCase listGoalUseCase;
    private final ListBudgetLimitUseCase listBudgetLimitUseCase;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ChatClient chatClient;

    public DashboardController(
            ListTransactionsByUserUseCase listTransactionsByUserUseCase,
            ListGoalUseCase listGoalUseCase,
            ListBudgetLimitUseCase listBudgetLimitUseCase,
            AuthenticatedUserProvider authenticatedUserProvider,
            @Value("classpath:/prompts/system-message.st") Resource systemPrompt,
            ChatClient.Builder chatClientBuilder) {
        this.listTransactionsByUserUseCase = listTransactionsByUserUseCase;
        this.listGoalUseCase = listGoalUseCase;
        this.listBudgetLimitUseCase = listBudgetLimitUseCase;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt)
                .defaultTools(listTransactionsByUserUseCase, listGoalUseCase, listBudgetLimitUseCase).build();
    }

    @GetMapping
    public String get(@RequestParam String param) {
        return new String();
    }
    
}
