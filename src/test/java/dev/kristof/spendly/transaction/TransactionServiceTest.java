package dev.kristof.spendly.transaction;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.Category;
import dev.kristof.spendly.category.CategoryService;
import dev.kristof.spendly.common.exception.TransactionNotFoundException;
import dev.kristof.spendly.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final Long USER_ID = 42L;
    private static final Long TRANSACTION_ID = 10L;
    private static final Long CATEGORY_ID = 3L;

    private static final OffsetDateTime FROM =
            OffsetDateTime.parse("2026-07-01T00:00:00Z");

    private static final OffsetDateTime TO =
            OffsetDateTime.parse("2026-07-31T23:59:59Z");

    private final Pageable pageable = PageRequest.of(0, 10);

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TransactionService transactionService;

    private void mockLoggedInUser() {
        User user = mock(User.class);

        when(user.getId()).thenReturn(USER_ID);
        when(currentUserService.getLoggedInUser()).thenReturn(user);
    }

    private void mockOwnedTransaction(Transaction transaction) {
        mockLoggedInUser();

        when(transactionRepository.findByIdAndUserId(
                TRANSACTION_ID, USER_ID
        )).thenReturn(Optional.of(transaction));
    }

    // CREATE

    @Test
    void createTransaction_shouldSaveTransactionWithCorrectData() {
        User user = mock(User.class);
        Category category = mock(Category.class);
        BigDecimal amount = new BigDecimal("15000.00");

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenReturn(category);

        when(currentUserService.getLoggedInUser())
                .thenReturn(user);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(
                CATEGORY_ID,
                amount,
                "Grocery shopping",
                FROM
        );

        assertNotNull(result);
        assertSame(category, result.getCategory());
        assertEquals(amount, result.getAmount());
        assertEquals("Grocery shopping", result.getDescription());
        assertEquals(FROM, result.getTransactionDate());

        assertSame(
                user,
                ReflectionTestUtils.getField(result, "user")
        );

        verify(categoryService).getAccessibleCategoryById(CATEGORY_ID);
        verify(transactionRepository).save(any(Transaction.class));
    }

    // LIST / FILTERS

    @Test
    void getOwnedTransactions_shouldFilterByCurrentUser() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository.findByUserId(USER_ID, pageable))
                .thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                null, null, null, null, pageable
        );

        assertSame(expected, result);

        verify(transactionRepository).findByUserId(USER_ID, pageable);
    }

    @Test
    void getOwnedTransactions_shouldFilterByDateRange() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository.findByUserIdAndTransactionDateBetween(
                USER_ID, FROM, TO, pageable
        )).thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                FROM, TO, null, null, pageable
        );

        assertSame(expected, result);
    }

    @Test
    void getOwnedTransactions_shouldFilterByCategory() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository.findByUserIdAndCategoryId(
                USER_ID, CATEGORY_ID, pageable
        )).thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                null, null, CATEGORY_ID, null, pageable
        );

        assertSame(expected, result);
    }

    @Test
    void getOwnedTransactions_shouldFilterBySearch() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository
                .findByUserIdAndDescriptionContainingIgnoreCase(
                        USER_ID, "shopping", pageable
                )).thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                null, null, null, "shopping", pageable
        );

        assertSame(expected, result);
    }

    @Test
    void getOwnedTransactions_shouldFilterByDateRangeAndCategory() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository
                .findByUserIdAndCategoryIdAndTransactionDateBetween(
                        USER_ID, CATEGORY_ID, FROM, TO, pageable
                )).thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                FROM, TO, CATEGORY_ID, null, pageable
        );

        assertSame(expected, result);
    }

    @Test
    void getOwnedTransactions_shouldFilterByDateRangeAndSearch() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository
                .findByUserIdAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
                        USER_ID, FROM, TO, "shopping", pageable
                )).thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                FROM, TO, null, "shopping", pageable
        );

        assertSame(expected, result);
    }

    @Test
    void getOwnedTransactions_shouldFilterByCategoryAndSearch() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository
                .findByUserIdAndCategoryIdAndDescriptionContainingIgnoreCase(
                        USER_ID, CATEGORY_ID, "shopping", pageable
                )).thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                null, null, CATEGORY_ID, "shopping", pageable
        );

        assertSame(expected, result);
    }

    @Test
    void getOwnedTransactions_shouldApplyAllFilters() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository
                .findByUserIdAndCategoryIdAndTransactionDateBetweenAndDescriptionContainingIgnoreCase(
                        USER_ID,
                        CATEGORY_ID,
                        FROM,
                        TO,
                        "shopping",
                        pageable
                )).thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                FROM, TO, CATEGORY_ID, "shopping", pageable
        );

        assertSame(expected, result);
    }

    @Test
    void getOwnedTransactions_shouldIgnoreBlankSearch() {
        mockLoggedInUser();

        Page<Transaction> expected = Page.empty();

        when(transactionRepository.findByUserId(USER_ID, pageable))
                .thenReturn(expected);

        Page<Transaction> result = transactionService.getOwnedTransactions(
                null, null, null, "   ", pageable
        );

        assertSame(expected, result);

        verify(transactionRepository).findByUserId(USER_ID, pageable);
    }

    // GET BY ID / OWNERSHIP

    @Test
    void getTransactionById_shouldReturnOwnedTransaction() {
        Transaction transaction = mock(Transaction.class);

        mockOwnedTransaction(transaction);

        Transaction result =
                transactionService.getTransactionById(TRANSACTION_ID);

        assertSame(transaction, result);

        verify(transactionRepository).findByIdAndUserId(
                TRANSACTION_ID, USER_ID
        );
    }

    @Test
    void getTransactionById_shouldRejectTransactionNotOwnedByUser() {
        mockLoggedInUser();

        when(transactionRepository.findByIdAndUserId(
                TRANSACTION_ID, USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(TRANSACTION_ID)
        );

        verify(transactionRepository).findByIdAndUserId(
                TRANSACTION_ID, USER_ID
        );
    }

    // UPDATE

    @Test
    void updateTransaction_shouldUpdateAllFields() {
        Transaction transaction = mock(Transaction.class);
        Category updatedCategory = mock(Category.class);

        mockOwnedTransaction(transaction);

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenReturn(updatedCategory);

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        BigDecimal updatedAmount = new BigDecimal("25000.00");

        Transaction result = transactionService.updateTransaction(
                TRANSACTION_ID,
                CATEGORY_ID,
                updatedAmount,
                "Updated description",
                TO
        );

        assertSame(transaction, result);

        verify(transaction).setCategory(updatedCategory);
        verify(transaction).setAmount(updatedAmount);
        verify(transaction).setDescription("Updated description");
        verify(transaction).setTransactionDate(TO);

        verify(categoryService).getAccessibleCategoryById(CATEGORY_ID);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void updateTransaction_shouldKeepExistingValuesWhenNull() {
        Transaction transaction = mock(Transaction.class);
        Category originalCategory = mock(Category.class);
        BigDecimal originalAmount = new BigDecimal("10000.00");

        mockOwnedTransaction(transaction);

        when(transaction.getCategory()).thenReturn(originalCategory);
        when(transaction.getAmount()).thenReturn(originalAmount);
        when(transaction.getDescription()).thenReturn("Original");
        when(transaction.getTransactionDate()).thenReturn(FROM);

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result = transactionService.updateTransaction(
                TRANSACTION_ID,
                null,
                null,
                null,
                null
        );

        assertSame(transaction, result);

        verify(transaction).setCategory(originalCategory);
        verify(transaction).setAmount(originalAmount);
        verify(transaction).setDescription("Original");
        verify(transaction).setTransactionDate(FROM);

        verifyNoInteractions(categoryService);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void updateTransaction_shouldRejectTransactionNotOwnedByUser() {
        mockLoggedInUser();

        when(transactionRepository.findByIdAndUserId(
                TRANSACTION_ID, USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.updateTransaction(
                        TRANSACTION_ID,
                        CATEGORY_ID,
                        BigDecimal.TEN,
                        "Changed",
                        TO
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verifyNoInteractions(categoryService);
    }

    // DELETE

    @Test
    void deleteTransaction_shouldDeleteOwnedTransaction() {
        Transaction transaction = mock(Transaction.class);

        mockOwnedTransaction(transaction);

        transactionService.deleteTransaction(TRANSACTION_ID);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_shouldRejectTransactionNotOwnedByUser() {
        mockLoggedInUser();

        when(transactionRepository.findByIdAndUserId(
                TRANSACTION_ID, USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.deleteTransaction(TRANSACTION_ID)
        );

        verify(transactionRepository, never())
                .delete(any(Transaction.class));
    }
}
