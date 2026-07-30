package dev.kristof.spendly.budget.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BudgetResponse {
    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal amount;
    private Integer year;
    private Integer month;

    public BudgetResponse(Long id, Long userId, Long categoryId, BigDecimal amount, Integer year, Integer month) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.year = year;
        this.month = month;
    }
}


