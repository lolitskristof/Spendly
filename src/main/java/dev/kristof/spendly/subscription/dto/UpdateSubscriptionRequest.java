package dev.kristof.spendly.subscription.dto;

import dev.kristof.spendly.subscription.BillingPeriod;
import dev.kristof.spendly.subscription.BillingStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class UpdateSubscriptionRequest {

    private Long categoryId;

    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private BillingPeriod billingPeriod;

    private LocalDate nextBillingDate;

    private BillingStatus status;
}