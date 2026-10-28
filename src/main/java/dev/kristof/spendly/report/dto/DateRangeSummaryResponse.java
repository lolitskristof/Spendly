package dev.kristof.spendly.report.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DateRangeSummaryResponse {
    private final BigDecimal income;
    private final BigDecimal expense;
    private final BigDecimal netAmount;

    public DateRangeSummaryResponse(BigDecimal income, BigDecimal expense, BigDecimal netAmount) {
        this.income = income;
        this.expense = expense;
        this.netAmount = netAmount;
    }


}
