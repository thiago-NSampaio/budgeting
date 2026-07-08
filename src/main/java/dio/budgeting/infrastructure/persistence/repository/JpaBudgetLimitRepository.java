package dio.budgeting.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import dio.budgeting.domain.balance.BudgetLimit;
import dio.budgeting.domain.balance.BudgetLimitRepository;
import dio.budgeting.domain.user.UserId;
import dio.budgeting.infrastructure.persistence.entity.BudgetLimitEntity;

@Repository
public class JpaBudgetLimitRepository implements BudgetLimitRepository{

    private BudgetLimitEntityRepository budgetLimitEntityRepository;

    public JpaBudgetLimitRepository(BudgetLimitEntityRepository budgetLimitEntityRepository) {
        this.budgetLimitEntityRepository = budgetLimitEntityRepository;
    }

    @Override
    public BudgetLimit save(BudgetLimit budgetLimit) {
        var entity = BudgetLimitEntity.from(budgetLimit);

        return budgetLimitEntityRepository.save(entity).toDomain();
    }

    @Override
    public Optional<BudgetLimit> findByUserId(UserId userId) {
        return budgetLimitEntityRepository.findByUserId(userId.uuid()).map(BudgetLimitEntity::toDomain);
    }

} 
