package dio.budgeting.application.transaction;

import org.springframework.stereotype.Service;

import dio.budgeting.application.input.PersistTransactionInput;
import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.transaction.Transaction;
import dio.budgeting.domain.transaction.TransactionRepository;
import dio.budgeting.providers.AuthenticatedUserProvider;

import org.springframework.ai.tool.annotation.Tool;;

@Service
public class PersistTransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public PersistTransactionUseCase(TransactionRepository transactionRepository, AuthenticatedUserProvider authenticatedUserProvider){
        this.transactionRepository = transactionRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Tool(name = "persist-transaction" ,description = "Persiste uma nova transação financeira")
    public TransactionOutput execute(PersistTransactionInput input){
        var userId = authenticatedUserProvider.currentUserId();

        var transaction = transactionRepository.save(new Transaction(input.description(), input.amount(), input.category(), userId, input.type()));
        return TransactionOutput.from(transaction);
    }
}
