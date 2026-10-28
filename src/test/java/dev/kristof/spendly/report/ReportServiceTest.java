package dev.kristof.spendly.report;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.Category;
import dev.kristof.spendly.category.CategoryType;
import dev.kristof.spendly.report.dto.DateRangeSummaryResponse;
import dev.kristof.spendly.transaction.Transaction;
import dev.kristof.spendly.transaction.TransactionRepository;
import dev.kristof.spendly.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private ReportService reportService;

    private final OffsetDateTime from =
            OffsetDateTime.parse("2026-07-01T00:00:00Z");

    private final OffsetDateTime to =
            OffsetDateTime.parse("2026-07-31T23:59:59Z");

    private void mockLoggedInUser() {
        User user = mock(User.class);

        when(user.getId()).thenReturn(1L);
        when(currentUserService.getLoggedInUser()).thenReturn(user);
    }

    @Test
    void getDateRangeSummary_shouldCalculateCorrectTotals() {
        mockLoggedInUser();

        when(transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(
                1L, from, to, CategoryType.INCOME
        )).thenReturn(new BigDecimal("150000.00"));

        when(transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(
                1L, from, to, CategoryType.EXPENSE
        )).thenReturn(new BigDecimal("60000.00"));

        DateRangeSummaryResponse result =
                reportService.getDateRangeSummary(from, to);

        assertEquals(new BigDecimal("150000.00"), result.getIncome());
        assertEquals(new BigDecimal("60000.00"), result.getExpense());
        assertEquals(new BigDecimal("90000.00"), result.getNetAmount());
    }

    @Test
    void getDateRangeSummary_shouldReturnZeroWhenNoTransactions() {
        mockLoggedInUser();

        when(transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(
                1L, from, to, CategoryType.INCOME
        )).thenReturn(BigDecimal.ZERO);

        when(transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(
                1L, from, to, CategoryType.EXPENSE
        )).thenReturn(BigDecimal.ZERO);

        DateRangeSummaryResponse result =
                reportService.getDateRangeSummary(from, to);

        assertEquals(BigDecimal.ZERO, result.getIncome());
        assertEquals(BigDecimal.ZERO, result.getExpense());
        assertEquals(BigDecimal.ZERO, result.getNetAmount());
    }

    @Test
    void getDateRangeSummary_shouldRejectNullDates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.getDateRangeSummary(null, to)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.getDateRangeSummary(from, null)
        );

        verifyNoInteractions(currentUserService, transactionRepository);
    }

    @Test
    void getDateRangeSummary_shouldRejectInvalidDateRange() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reportService.getDateRangeSummary(to, from)
        );

        assertEquals(
                "End date cannot be before start date",
                exception.getMessage()
        );

        verifyNoInteractions(currentUserService, transactionRepository);
    }

    @Test
    void getMonthlyReport_shouldUseCorrectMonthBoundaries() {
        mockLoggedInUser();

        OffsetDateTime monthStart =
                OffsetDateTime.parse("2026-07-01T00:00:00Z");

        OffsetDateTime monthEnd =
                OffsetDateTime.parse("2026-07-31T23:59:59.999999Z");

        when(transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(
                1L, monthStart, monthEnd, CategoryType.INCOME
        )).thenReturn(new BigDecimal("100000.00"));

        when(transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(
                1L, monthStart, monthEnd, CategoryType.EXPENSE
        )).thenReturn(new BigDecimal("25000.00"));

        DateRangeSummaryResponse result =
                reportService.getMonthlyReport(2026, 7);

        assertEquals(new BigDecimal("75000.00"), result.getNetAmount());

        verify(transactionRepository).sumAmountByUserAndDateRangeAndCategoryType(
                1L, monthStart, monthEnd, CategoryType.INCOME
        );

        verify(transactionRepository).sumAmountByUserAndDateRangeAndCategoryType(
                1L, monthStart, monthEnd, CategoryType.EXPENSE
        );
    }

    @Test
    void exportTransactionsToCsv_shouldReturnHeaderWhenEmpty() {
        mockLoggedInUser();

        when(transactionRepository.findTransactionsForExport(
                1L, from, to
        )).thenReturn(List.of());

        String csv = reportService.exportTransactionsToCsv(from, to);

        assertEquals(
                "Date,Type,Category,Amount,Description\n",
                csv
        );
    }

    @Test
    void exportTransactionsToCsv_shouldFormatTransactionsCorrectly() {
        mockLoggedInUser();

        Transaction transaction = mock(Transaction.class);
        Category category = mock(Category.class);

        when(transactionRepository.findTransactionsForExport(
                1L, from, to
        )).thenReturn(List.of(transaction));

        when(transaction.getTransactionDate()).thenReturn(
                OffsetDateTime.parse("2026-07-25T18:30:00Z")
        );

        when(transaction.getCategory()).thenReturn(category);
        when(category.getCategoryType()).thenReturn(CategoryType.EXPENSE);
        when(category.getName()).thenReturn("Health");
        when(transaction.getAmount()).thenReturn(new BigDecimal("20000.00"));
        when(transaction.getDescription()).thenReturn("Doctor appointment");

        String csv = reportService.exportTransactionsToCsv(from, to);

        String expected =
                "Date,Type,Category,Amount,Description\n"
                        + "2026-07-25T18:30Z,EXPENSE,\"Health\",20000.00,\"Doctor appointment\"\n";

        assertEquals(expected, csv);
    }

    @Test
    void exportTransactionsToCsv_shouldEscapeSpecialCharacters() {
        mockLoggedInUser();

        Transaction transaction = mock(Transaction.class);
        Category category = mock(Category.class);

        when(transactionRepository.findTransactionsForExport(
                1L, from, to
        )).thenReturn(List.of(transaction));

        when(transaction.getTransactionDate()).thenReturn(from);
        when(transaction.getCategory()).thenReturn(category);
        when(category.getCategoryType()).thenReturn(CategoryType.EXPENSE);
        when(category.getName()).thenReturn("Food, Drinks");
        when(transaction.getAmount()).thenReturn(new BigDecimal("5000.00"));
        when(transaction.getDescription()).thenReturn("Shopping \"weekly\"");

        String csv = reportService.exportTransactionsToCsv(from, to);

        assertTrue(csv.contains("\"Food, Drinks\""));
        assertTrue(csv.contains("\"Shopping \"\"weekly\"\"\""));
    }

    @Test
    void exportTransactionsToCsv_shouldPreventBasicFormulaInjection() {
        mockLoggedInUser();

        Transaction transaction = mock(Transaction.class);
        Category category = mock(Category.class);

        when(transactionRepository.findTransactionsForExport(
                1L, from, to
        )).thenReturn(List.of(transaction));

        when(transaction.getTransactionDate()).thenReturn(from);
        when(transaction.getCategory()).thenReturn(category);
        when(category.getCategoryType()).thenReturn(CategoryType.EXPENSE);
        when(category.getName()).thenReturn("Shopping");
        when(transaction.getAmount()).thenReturn(BigDecimal.ONE);
        when(transaction.getDescription()).thenReturn("=SUM(1,2)");

        String csv = reportService.exportTransactionsToCsv(from, to);

        assertTrue(csv.contains("\"'=SUM(1,2)\""));
    }

    @Test
    void exportTransactionsToCsv_shouldRejectInvalidDateRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.exportTransactionsToCsv(to, from)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reportService.exportTransactionsToCsv(null, to)
        );

        verifyNoInteractions(currentUserService, transactionRepository);
    }
}
