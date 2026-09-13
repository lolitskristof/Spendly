package dev.kristof.spendly.dashboard.dto;


import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MonthlySummaryResponse {
    private BigDecimal income;
    private BigDecimal expenses;
}
