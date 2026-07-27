package dev.kristof.spendly.transaction;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.Category;
import dev.kristof.spendly.category.CategoryService;
import dev.kristof.spendly.common.exception.TransactionNotFoundException;
import dev.kristof.spendly.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;

    public TransactionService(TransactionRepository transactionRepository, CategoryService categoryService, CurrentUserService currentUserService) {
        this.transactionRepository = transactionRepository;
        this.categoryService = categoryService;
        this.currentUserService = currentUserService;
    }

    public Transaction createTransaction(Long categoryId, BigDecimal amount, String description, OffsetDateTime transactionDate) {
        Category category = categoryService.getAccessibleCategoryById(categoryId);
        User user = currentUserService.getLoggedInUser();

        return transactionRepository.save(new Transaction(user, category, amount, description, transactionDate));
    }

    public Page<Transaction> getOwnedTransactions(
            OffsetDateTime from,
            OffsetDateTime to,
            Long categoryId,
            String search,
            Pageable pageable
    ) {
        Long userId = currentUserService.getLoggedInUser().getId();
        boolean hasDateRange = from != null && to != null;
        boolean hasCategory = categoryId != null;
        boolean hasSearch = search != null && !search.isBlank();

        if (hasDateRange && hasCategory && hasSearch) {
            return transactionRepository
                    .findByUserIdAndCategoryIdAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
                            userId,
                            categoryId,
                            from,
                            to,
                            search,
                            pageable
                    );
        }

        if (hasDateRange && hasSearch) {
            return transactionRepository
                    .findByUserIdAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
                            userId,
                            from,
                            to,
                            search,
                            pageable
                    );
        }

        if (hasCategory && hasSearch) {
            return transactionRepository
                    .findByUserIdAndCategoryIdAndDescriptionContainingIgnoreCase(
                            userId,
                            categoryId,
                            search,
                            pageable
                    );
        }

        if (hasSearch) {
            return transactionRepository
                    .findByUserIdAndDescriptionContainingIgnoreCase(
                            userId,
                            search,
                            pageable
                    );
        }

        if (hasDateRange && hasCategory) {
            return transactionRepository
                    .findByUserIdAndCategoryIdAndTransactionDateBetween(
                            userId,
                            categoryId,
                            from,
                            to,
                            pageable
                    );
        }

        if (hasDateRange) {
            return transactionRepository
                    .findByUserIdAndTransactionDateBetween(
                            userId,
                            from,
                            to,
                            pageable
                    );
        }

        if (hasCategory) {
            return transactionRepository
                    .findByUserIdAndCategoryId(
                            userId,
                            categoryId,
                            pageable
                    );
        }

        return transactionRepository.findByUserId(userId, pageable);
    }

    public Transaction getTransactionById(Long transactionId) {
        Long userId = currentUserService.getLoggedInUser().getId();

        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Cannot find transaction with id: " + transactionId
                        )
                );
    }

    public Transaction updateTransaction(Long transactionId, Long categoryId, BigDecimal amount, String description, OffsetDateTime transactionDate) {
        Transaction transaction = getTransactionById(transactionId);
        Category updatedCategory = categoryId == null
                ? transaction.getCategory()
                : categoryService.getAccessibleCategoryById(categoryId);

        BigDecimal updatedAmount = amount == null ? transaction.getAmount() : amount;
        String updatedDescription = description == null ? transaction.getDescription() : description;
        OffsetDateTime updatedTransactionDate = transactionDate == null ? transaction.getTransactionDate() : transactionDate;

        transaction.setCategory(updatedCategory);
        transaction.setAmount(updatedAmount);
        transaction.setDescription(updatedDescription);
        transaction.setTransactionDate(updatedTransactionDate);

        return transactionRepository.save(transaction);
    }

    public void deleteTransaction(Long transactionId) {
        Transaction transaction = getTransactionById(transactionId);
        transactionRepository.delete(transaction);
    }
}
