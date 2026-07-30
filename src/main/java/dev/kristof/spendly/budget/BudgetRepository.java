package dev.kristof.spendly.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    boolean existsByUserIdAndCategoryIdAndYearAndMonth(
            Long userId,
            Long categoryId,
            Integer year,
            Integer month
    );

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByIdAndUserId(Long budgetId, Long userId);

    List<Budget> findByUserIdAndYearAndMonth(
            Long userId,
            Integer year,
            Integer month
    );

    boolean existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
            Long userId,
            Long categoryId,
            Integer year,
            Integer month,
            Long budgetId
    );


}