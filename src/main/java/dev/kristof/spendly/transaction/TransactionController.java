package dev.kristof.spendly.transaction;

import dev.kristof.spendly.transaction.dto.CreateTransactionRequest;
import dev.kristof.spendly.transaction.dto.TransactionResponse;
import dev.kristof.spendly.transaction.dto.UpdateTransactionRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public TransactionResponse createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(request.getCategoryId(), request.getAmount(), request.getDescription(), request.getTransactionDate());

        return new TransactionResponse(transaction.getId(), transaction.getCategory().getId(), transaction.getUser().getId(), transaction.getAmount(), transaction.getDescription(), transaction.getTransactionDate());
    }

    @GetMapping
    public Page<TransactionResponse> getOwnedTransactions(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,

            @RequestParam(required = false)
            Long categoryId,

            @RequestParam(required = false)
            String search,

            Pageable pageable
    ) {
        return transactionService
                .getOwnedTransactions(from, to, categoryId, search, pageable)
                .map(transaction -> new TransactionResponse(
                        transaction.getId(),
                        transaction.getCategory().getId(),
                        transaction.getUser().getId(),
                        transaction.getAmount(),
                        transaction.getDescription(),
                        transaction.getTransactionDate()
                ));
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse getTransaction(@PathVariable Long transactionId) {
        Transaction transaction = transactionService.getTransactionById(transactionId);
        return new TransactionResponse(transaction.getId(), transaction.getCategory().getId(), transaction.getUser().getId(), transaction.getAmount(), transaction.getDescription(), transaction.getTransactionDate());
    }

    @PatchMapping("/{transactionId}")
    public TransactionResponse updateTransaction(@PathVariable Long transactionId, @Valid @RequestBody UpdateTransactionRequest request) {
        Transaction transaction = transactionService.updateTransaction(transactionId, request.getCategoryId(), request.getAmount(), request.getDescription(), request.getTransactionDate());

        return new TransactionResponse(transaction.getId(), transaction.getCategory().getId(), transaction.getUser().getId(), transaction.getAmount(), transaction.getDescription(), transaction.getTransactionDate());
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long transactionId) {
        transactionService.deleteTransaction(transactionId);
        return ResponseEntity.noContent().build();
    }

}
