package dev.kristof.spendly.category.dto;

import dev.kristof.spendly.category.CategoryType;
import lombok.Getter;

@Getter
public class CategoryResponse {
    private final Long id;
    private final String name;
    private final CategoryType categoryType;
    private final Long userId;


    public CategoryResponse(Long id, Long userId, CategoryType categoryType, String name) {
        this.id = id;
        this.userId = userId;
        this.categoryType = categoryType;
        this.name = name;
    }
}
