package dio.budgeting.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.Transaction;
import dio.budgeting.domain.TransactionRepository;
import dio.budgeting.domain.UserId;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;

@Repository
public class JpaTransactionRepository implements TransactionRepository{

    private final TransactionEntityRepository transactionEntityRepository;

    public JpaTransactionRepository(TransactionEntityRepository transactionEntityRepository){
        this.transactionEntityRepository = transactionEntityRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        var entity = TransactionEntity.from(transaction);
        return transactionEntityRepository.save(entity).toDomain();
    }
    
    @Override
    public List<Transaction> findByCategoryAndUserId(Category category, UserId userId) {
        return transactionEntityRepository.findByCategoryAndUserId(category, userId.uuid()).stream().map(TransactionEntity::toDomain).toList();
    }

    @Override
    public List<Transaction> findByUserId(UserId userId) {
        return transactionEntityRepository.findByUserId( userId.uuid()).stream().map(TransactionEntity::toDomain).toList();
    }
}
