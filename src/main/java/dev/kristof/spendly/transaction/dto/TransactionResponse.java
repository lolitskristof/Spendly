package dev.kristof.spendly.transaction.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class TransactionResponse {
    private Long id;
    private Long categoryId;
    private Long userId;
    private BigDecimal amount;
    private String description;
    private OffsetDateTime transactionDate;

    public TransactionResponse(Long id, Long categoryId, Long userId, BigDecimal amount, String description, OffsetDateTime transactionDate) {
        this.id = id;
        this.categoryId = categoryId;
        this.userId = userId;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
    }
}
