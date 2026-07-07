package dio.budgeting.infrastructure.http;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dio.budgeting.application.ListDashboardUseCase;
import dio.budgeting.application.output.DashboardOutput;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final ListDashboardUseCase listDashboardUseCase;
    private final ChatClient chatClient;

    public DashboardController(
            ListDashboardUseCase listDashboardUseCase,
            @Value("classpath:/prompts/dashboard-insight.st") Resource dashboardInsight,
            ChatClient.Builder chatClientBuilder) {

        this.listDashboardUseCase = listDashboardUseCase;
        this.chatClient = chatClientBuilder.defaultSystem(dashboardInsight)
                .defaultTools(listDashboardUseCase).build();
    }

    @GetMapping
    public ResponseEntity<Object> stats() {
        DashboardOutput dashboard = listDashboardUseCase.execute();

        String insight = chatClient.prompt()
                .user(dashboard.toAiSummary())
                .call()
                .content();

        return ResponseEntity.ok(dashboard.withInsight(insight));
    }
}
