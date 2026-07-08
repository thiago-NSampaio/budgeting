package dio.budgeting.application.transaction;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.transaction.TransactionRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

@Service
public class ListTransactionsByCategoryUseCase {
    private final TransactionRepository transactionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ListTransactionsByCategoryUseCase(TransactionRepository transactionRepository, AuthenticatedUserProvider authenticatedUserProvider){
        this.transactionRepository = transactionRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Tool(name = "list-transactions-by-category" ,description = "Lista transações financeiras por categoria")
    public List<TransactionOutput> execute(@ToolParam(description = "Categoria de uma transação") Category category){
        var userId = authenticatedUserProvider.currentUserId();

        return transactionRepository.findByCategoryAndUserId(category, userId).stream().map(TransactionOutput::from).toList();
    }
}
