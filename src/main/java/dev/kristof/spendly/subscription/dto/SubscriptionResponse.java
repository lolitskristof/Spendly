package dev.kristof.spendly.subscription.dto;

import dev.kristof.spendly.subscription.BillingPeriod;
import dev.kristof.spendly.subscription.BillingStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class SubscriptionResponse {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String name;
    private BigDecimal amount;
    private BillingPeriod period;
    private LocalDate nextBillingDate;
    private BillingStatus status;

    public SubscriptionResponse(Long id, Long userId, Long categoryId, String name, BigDecimal amount, BillingPeriod period, LocalDate nextBillingDate, BillingStatus status) {
        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.name = name;
        this.amount = amount;
        this.period = period;
        this.nextBillingDate = nextBillingDate;
        this.status = status;
    }
}
