package dio.budgeting.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import dio.budgeting.domain.BudgetLimit;
import dio.budgeting.domain.BudgetLimitRepository;
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

} 
