package dio.budgeting.infrastructure.persistence.repository;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import dio.budgeting.infrastructure.persistence.entity.BudgetLimitEntity;

public interface BudgetLimitEntityRepository extends CrudRepository<BudgetLimitEntity, UUID>{
    
}
