package dio.budgeting.domain.chatMemory;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResult {
    private String assistantText;
    private List<ActionTaken> actionsTaken;
    private Error error;
    private RequestConfirmation confirmation;
    private Clarification clarification;
    private List<String> toolsCalled;
    private String status;
}
