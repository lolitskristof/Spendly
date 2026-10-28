package dev.kristof.spendly.dashboard.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BudgetStatusResponse {
    private final Long budgetId;
    private final Long categoryId;
    private final String categoryName;
    private final BigDecimal budgetAmount;
    private final BigDecimal spentAmount;
    private final BigDecimal remainingAmount;
    private final boolean exceeded;

    public BudgetStatusResponse(Long budgetId, Long categoryId, String categoryName, BigDecimal budgetAmount, BigDecimal spentAmount, BigDecimal remainingAmount, boolean exceeded) {
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.remainingAmount = remainingAmount;
        this.exceeded = exceeded;
    }
}
