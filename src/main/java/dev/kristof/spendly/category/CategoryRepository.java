package dev.kristof.spendly.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIsNullOrUserId(Long userId);

    boolean existsByUserIdAndNameIgnoreCaseAndCategoryType(
            Long userId,
            String name,
            CategoryType categoryType
    );

    boolean existsByUserIdAndNameIgnoreCaseAndCategoryTypeAndIdNot(
            Long userId,
            String name,
            CategoryType categoryType,
            Long categoryId
    );

    boolean existsByUserIsNullAndNameIgnoreCaseAndCategoryTypeAndIdNot(
            String name,
            CategoryType categoryType,
            Long categoryId
    );
}