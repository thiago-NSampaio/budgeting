package dio.budgeting.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import dio.budgeting.domain.Category;
import dio.budgeting.domain.TransactionType;
import dio.budgeting.infrastructure.persistence.entity.TransactionEntity;

public interface TransactionEntityRepository extends CrudRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findByCategoryAndUserId(Category category, UUID userId);

    List<TransactionEntity> findByUserId(UUID userId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM TransactionEntity t
            WHERE t.userId = :userId
              AND t.type = :type
            """)
    Long sumByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type);
}