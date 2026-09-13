package dev.kristof.spendly.transaction;

import dev.kristof.spendly.category.CategoryType;
import dev.kristof.spendly.dashboard.dto.CategoryExpenseSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(Long transactionId, Long userId);

    Page<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryId(
            Long userId,
            Long categoryId,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryIdAndTransactionDateBetween(
            Long userId,
            Long categoryId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndDescriptionContainingIgnoreCase(
            Long userId,
            String search,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryIdAndDescriptionContainingIgnoreCase(
            Long userId,
            Long categoryId,
            String search,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
            Long userId,
            OffsetDateTime from,
            OffsetDateTime to,
            String search,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryIdAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
            Long userId,
            Long categoryId,
            OffsetDateTime from,
            OffsetDateTime to,
            String search,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user.id = :userId
              AND t.transactionDate BETWEEN :from AND :to
              AND t.category.categoryType = :categoryType
            """)
    BigDecimal sumAmountByUserAndDateRangeAndCategoryType(
            Long userId,
            OffsetDateTime from,
            OffsetDateTime to,
            CategoryType categoryType
    );

    @Query("""
            SELECT new dev.kristof.spendly.dashboard.dto.CategoryExpenseSummary(
                t.category.id,
                t.category.name,
                SUM(t.amount)
            )
            FROM Transaction t
            WHERE t.user.id = :userId
              AND t.transactionDate >= :from
              AND t.transactionDate < :to
              AND t.category.categoryType = :categoryType
            GROUP BY t.category.id, t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryExpenseSummary> getExpenseSummaryByCategory(
            @Param("userId") Long userId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("categoryType") CategoryType categoryType
    );
}