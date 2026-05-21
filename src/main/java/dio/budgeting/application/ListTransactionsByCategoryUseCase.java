package dio.budgeting.application;

import java.util.List;

import org.springframework.stereotype.Service;

import dio.budgeting.application.output.TransactionOutput;
import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionRepository;

@Service
public class ListTransactionsByCategoryUseCase {
    private final TransactionRepository transactionRepository;

    public ListTransactionsByCategoryUseCase(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionOutput> execute(Category category){
        return transactionRepository.findAllByCategory(category).stream().map(TransactionOutput::from).toList();
    }
}
