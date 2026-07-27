package dev.kristof.spendly.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(Long transactionId, Long userId);

    Page<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryId(
            Long userId,
            Long categoryId,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryIdAndTransactionDateBetween(
            Long userId,
            Long categoryId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndDescriptionContainingIgnoreCase(
            Long userId,
            String search,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryIdAndDescriptionContainingIgnoreCase(
            Long userId,
            Long categoryId,
            String search,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
            Long userId,
            OffsetDateTime from,
            OffsetDateTime to,
            String search,
            Pageable pageable
    );

    Page<Transaction> findByUserIdAndCategoryIdAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
            Long userId,
            Long categoryId,
            OffsetDateTime from,
            OffsetDateTime to,
            String search,
            Pageable pageable
    );
}