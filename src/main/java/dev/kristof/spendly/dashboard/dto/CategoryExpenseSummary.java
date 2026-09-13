package dev.kristof.spendly.dashboard.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CategoryExpenseSummary {

    private final Long categoryId;
    private final String categoryName;
    private final BigDecimal amount;

    public CategoryExpenseSummary(
            Long categoryId,
            String categoryName,
            BigDecimal amount
    ) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.amount = amount;
    }
}