package dev.kristof.spendly.category.dto;

import dev.kristof.spendly.category.CategoryType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {
    String name;
    CategoryType categoryType;
}
