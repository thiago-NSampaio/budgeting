package dio.budgeting.application.transaction;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.transaction.TransactionRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class ListTransactionsByUserUseCase {
    private final TransactionRepository transactionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ListTransactionsByUserUseCase(TransactionRepository transactionRepository,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.transactionRepository = transactionRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Tool(name = "list-transactions-by-user" ,description = "Lista transações financeiras do usuário")
    public List<TransactionOutput> execute(){
        var userId = authenticatedUserProvider.currentUserId();

        return transactionRepository.findByUserId(userId).stream().map(TransactionOutput::from).toList();
    }
}
