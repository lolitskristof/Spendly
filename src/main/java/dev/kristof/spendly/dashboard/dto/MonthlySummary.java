package dev.kristof.spendly.dashboard.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MonthlySummary {

    private final BigDecimal income;
    private final BigDecimal expense;
    private final BigDecimal monthlyBalance;

    public MonthlySummary(BigDecimal income, BigDecimal expense, BigDecimal monthlyBalance) {
        this.income = income;
        this.expense = expense;
        this.monthlyBalance = monthlyBalance;
    }
}