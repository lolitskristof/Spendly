package dev.kristof.spendly.subscription.dto;

import dev.kristof.spendly.subscription.BillingPeriod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateSubscriptionRequest {

    @NotNull(message = "Category id is required")
    private Long categoryId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Billing period is required")
    private BillingPeriod billingPeriod;

    @NotNull(message = "Next billing date is required")
    private LocalDate nextBillingDate;
}