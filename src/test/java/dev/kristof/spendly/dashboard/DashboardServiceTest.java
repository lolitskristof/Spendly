package dev.kristof.spendly.dashboard;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.budget.Budget;
import dev.kristof.spendly.budget.BudgetRepository;
import dev.kristof.spendly.category.Category;
import dev.kristof.spendly.category.CategoryType;
import dev.kristof.spendly.dashboard.dto.BudgetStatusResponse;
import dev.kristof.spendly.dashboard.dto.CategoryExpenseSummary;
import dev.kristof.spendly.dashboard.dto.MonthlySummary;
import dev.kristof.spendly.subscription.BillingStatus;
import dev.kristof.spendly.subscription.Subscription;
import dev.kristof.spendly.subscription.SubscriptionRepository;
import dev.kristof.spendly.transaction.TransactionRepository;
import dev.kristof.spendly.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final Long USER_ID = 42L;
    private static final Integer YEAR = 2026;
    private static final Integer MONTH = 7;

    private static final OffsetDateTime MONTH_START =
            OffsetDateTime.parse("2026-07-01T00:00:00Z");

    private static final OffsetDateTime MONTH_END_INCLUSIVE =
            OffsetDateTime.parse("2026-07-31T23:59:59.999999999Z");

    private static final OffsetDateTime NEXT_MONTH_START =
            OffsetDateTime.parse("2026-08-01T00:00:00Z");

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private void mockLoggedInUser() {
        User user = mock(User.class);

        when(user.getId()).thenReturn(USER_ID);
        when(currentUserService.getLoggedInUser()).thenReturn(user);
    }

    private Budget mockBudget(
            Long budgetId,
            Long categoryId,
            String categoryName,
            BigDecimal amount
    ) {
        Budget budget = mock(Budget.class);
        Category category = mock(Category.class);

        when(budget.getId()).thenReturn(budgetId);
        when(budget.getCategory()).thenReturn(category);
        when(budget.getAmount()).thenReturn(amount);

        when(category.getId()).thenReturn(categoryId);
        when(category.getName()).thenReturn(categoryName);

        return budget;
    }

    // MONTHLY SUMMARY

    @Test
    void getMonthlySummary_shouldCalculateCorrectTotals() {
        mockLoggedInUser();

        when(transactionRepository
                .sumAmountByUserAndDateRangeAndCategoryType(
                        USER_ID,
                        MONTH_START,
                        MONTH_END_INCLUSIVE,
                        CategoryType.INCOME
                )).thenReturn(new BigDecimal("150000.00"));

        when(transactionRepository
                .sumAmountByUserAndDateRangeAndCategoryType(
                        USER_ID,
                        MONTH_START,
                        MONTH_END_INCLUSIVE,
                        CategoryType.EXPENSE
                )).thenReturn(new BigDecimal("60000.00"));

        MonthlySummary result =
                dashboardService.getMonthlySummary(YEAR, MONTH);

        assertEquals(
                new BigDecimal("150000.00"),
                ReflectionTestUtils.getField(result, "income")
        );

        assertEquals(
                new BigDecimal("60000.00"),
                ReflectionTestUtils.getField(result, "expense")
        );

        assertEquals(
                new BigDecimal("90000.00"),
                ReflectionTestUtils.getField(result, "monthlyBalance")
        );
    }

    @Test
    void getMonthlySummary_shouldReturnZeroWithoutTransactions() {
        mockLoggedInUser();

        when(transactionRepository
                .sumAmountByUserAndDateRangeAndCategoryType(
                        USER_ID,
                        MONTH_START,
                        MONTH_END_INCLUSIVE,
                        CategoryType.INCOME
                )).thenReturn(BigDecimal.ZERO);

        when(transactionRepository
                .sumAmountByUserAndDateRangeAndCategoryType(
                        USER_ID,
                        MONTH_START,
                        MONTH_END_INCLUSIVE,
                        CategoryType.EXPENSE
                )).thenReturn(BigDecimal.ZERO);

        MonthlySummary result =
                dashboardService.getMonthlySummary(YEAR, MONTH);

        assertEquals(
                BigDecimal.ZERO,
                ReflectionTestUtils.getField(result, "monthlyBalance")
        );
    }

    @Test
    void getMonthlySummary_shouldHandleLeapYearFebruary() {
        mockLoggedInUser();

        OffsetDateTime from =
                OffsetDateTime.parse("2028-02-01T00:00:00Z");

        OffsetDateTime to =
                OffsetDateTime.parse("2028-02-29T23:59:59.999999999Z");

        when(transactionRepository
                .sumAmountByUserAndDateRangeAndCategoryType(
                        USER_ID, from, to, CategoryType.INCOME
                )).thenReturn(BigDecimal.TEN);

        when(transactionRepository
                .sumAmountByUserAndDateRangeAndCategoryType(
                        USER_ID, from, to, CategoryType.EXPENSE
                )).thenReturn(BigDecimal.ONE);

        MonthlySummary result =
                dashboardService.getMonthlySummary(2028, 2);

        assertEquals(
                new BigDecimal("9"),
                ReflectionTestUtils.getField(result, "monthlyBalance")
        );

        verify(transactionRepository)
                .sumAmountByUserAndDateRangeAndCategoryType(
                        USER_ID, from, to, CategoryType.INCOME
                );

        verify(transactionRepository)
                .sumAmountByUserAndDateRangeAndCategoryType(
                        USER_ID, from, to, CategoryType.EXPENSE
                );
    }

    @Test
    void getMonthlySummary_shouldRejectInvalidMonth() {
        when(currentUserService.getLoggedInUser())
                .thenReturn(mock(User.class));

        assertThrows(
                DateTimeException.class,
                () -> dashboardService.getMonthlySummary(2026, 13)
        );

        verifyNoInteractions(transactionRepository);
    }

    // CATEGORY EXPENSE SUMMARY

    @Test
    void getCategoryExpenseSummary_shouldUseCorrectFilters() {
        mockLoggedInUser();

        List<CategoryExpenseSummary> expected =
                List.of(mock(CategoryExpenseSummary.class));

        when(transactionRepository.getExpenseSummaryByCategory(
                USER_ID,
                MONTH_START,
                NEXT_MONTH_START,
                CategoryType.EXPENSE
        )).thenReturn(expected);

        List<CategoryExpenseSummary> result =
                dashboardService.getCategoryExpenseSummary(YEAR, MONTH);

        assertSame(expected, result);

        verify(transactionRepository).getExpenseSummaryByCategory(
                USER_ID,
                MONTH_START,
                NEXT_MONTH_START,
                CategoryType.EXPENSE
        );
    }

    @Test
    void getCategoryExpenseSummary_shouldReturnEmptyList() {
        mockLoggedInUser();

        when(transactionRepository.getExpenseSummaryByCategory(
                USER_ID,
                MONTH_START,
                NEXT_MONTH_START,
                CategoryType.EXPENSE
        )).thenReturn(List.of());

        List<CategoryExpenseSummary> result =
                dashboardService.getCategoryExpenseSummary(YEAR, MONTH);

        assertTrue(result.isEmpty());
    }

    // BUDGET STATUS

    @Test
    void getMonthlyBudgetStatus_shouldCalculateRemainingAmount() {
        mockLoggedInUser();

        Budget budget = mockBudget(
                10L,
                3L,
                "Food",
                new BigDecimal("100000.00")
        );

        when(budgetRepository.findByUserIdAndYearAndMonth(
                USER_ID, YEAR, MONTH
        )).thenReturn(List.of(budget));

        when(transactionRepository.sumSpentByCategory(
                USER_ID,
                3L,
                MONTH_START,
                NEXT_MONTH_START
        )).thenReturn(new BigDecimal("60000.00"));

        List<BudgetStatusResponse> result =
                dashboardService.getMonthlyBudgetStatus(YEAR, MONTH);

        assertEquals(1, result.size());

        BudgetStatusResponse status = result.get(0);

        assertEquals(
                new BigDecimal("100000.00"),
                ReflectionTestUtils.getField(status, "budgetAmount")
        );

        assertEquals(
                new BigDecimal("60000.00"),
                ReflectionTestUtils.getField(status, "spentAmount")
        );

        assertEquals(
                new BigDecimal("40000.00"),
                ReflectionTestUtils.getField(status, "remainingAmount")
        );

        assertEquals(
                false,
                ReflectionTestUtils.getField(status, "exceeded")
        );
    }

    @Test
    void getMonthlyBudgetStatus_shouldDetectExceededBudget() {
        mockLoggedInUser();

        Budget budget = mockBudget(
                10L,
                3L,
                "Food",
                new BigDecimal("50000.00")
        );

        when(budgetRepository.findByUserIdAndYearAndMonth(
                USER_ID, YEAR, MONTH
        )).thenReturn(List.of(budget));

        when(transactionRepository.sumSpentByCategory(
                USER_ID,
                3L,
                MONTH_START,
                NEXT_MONTH_START
        )).thenReturn(new BigDecimal("70000.00"));

        List<BudgetStatusResponse> result =
                dashboardService.getMonthlyBudgetStatus(YEAR, MONTH);

        BudgetStatusResponse status = result.get(0);

        assertEquals(
                new BigDecimal("-20000.00"),
                ReflectionTestUtils.getField(status, "remainingAmount")
        );

        assertEquals(
                true,
                ReflectionTestUtils.getField(status, "exceeded")
        );
    }

    @Test
    void getMonthlyBudgetStatus_shouldNotExceedWhenExactlyAtLimit() {
        mockLoggedInUser();

        Budget budget = mockBudget(
                10L,
                3L,
                "Food",
                new BigDecimal("50000.00")
        );

        when(budgetRepository.findByUserIdAndYearAndMonth(
                USER_ID, YEAR, MONTH
        )).thenReturn(List.of(budget));

        when(transactionRepository.sumSpentByCategory(
                USER_ID,
                3L,
                MONTH_START,
                NEXT_MONTH_START
        )).thenReturn(new BigDecimal("50000.00"));

        List<BudgetStatusResponse> result =
                dashboardService.getMonthlyBudgetStatus(YEAR, MONTH);

        BudgetStatusResponse status = result.get(0);

        assertEquals(
                BigDecimal.ZERO,
                ((BigDecimal) ReflectionTestUtils.getField(
                        status, "remainingAmount"
                ))
                        .stripTrailingZeros()
        );

        assertEquals(
                false,
                ReflectionTestUtils.getField(status, "exceeded")
        );
    }

    @Test
    void getMonthlyBudgetStatus_shouldKeepFullBudgetWhenNothingSpent() {
        mockLoggedInUser();

        Budget budget = mockBudget(
                10L,
                3L,
                "Food",
                new BigDecimal("50000.00")
        );

        when(budgetRepository.findByUserIdAndYearAndMonth(
                USER_ID, YEAR, MONTH
        )).thenReturn(List.of(budget));

        when(transactionRepository.sumSpentByCategory(
                USER_ID,
                3L,
                MONTH_START,
                NEXT_MONTH_START
        )).thenReturn(BigDecimal.ZERO);

        List<BudgetStatusResponse> result =
                dashboardService.getMonthlyBudgetStatus(YEAR, MONTH);

        BudgetStatusResponse status = result.get(0);

        assertEquals(
                new BigDecimal("50000.00"),
                ReflectionTestUtils.getField(status, "remainingAmount")
        );

        assertEquals(
                false,
                ReflectionTestUtils.getField(status, "exceeded")
        );
    }

    @Test
    void getMonthlyBudgetStatus_shouldCalculateEachCategorySeparately() {
        mockLoggedInUser();

        Budget foodBudget = mockBudget(
                10L,
                3L,
                "Food",
                new BigDecimal("50000.00")
        );

        Budget healthBudget = mockBudget(
                11L,
                4L,
                "Health",
                new BigDecimal("30000.00")
        );

        when(budgetRepository.findByUserIdAndYearAndMonth(
                USER_ID, YEAR, MONTH
        )).thenReturn(List.of(foodBudget, healthBudget));

        when(transactionRepository.sumSpentByCategory(
                USER_ID, 3L, MONTH_START, NEXT_MONTH_START
        )).thenReturn(new BigDecimal("20000.00"));

        when(transactionRepository.sumSpentByCategory(
                USER_ID, 4L, MONTH_START, NEXT_MONTH_START
        )).thenReturn(new BigDecimal("40000.00"));

        List<BudgetStatusResponse> result =
                dashboardService.getMonthlyBudgetStatus(YEAR, MONTH);

        assertEquals(2, result.size());

        assertEquals(
                new BigDecimal("30000.00"),
                ReflectionTestUtils.getField(
                        result.get(0), "remainingAmount"
                )
        );

        assertEquals(
                new BigDecimal("-10000.00"),
                ReflectionTestUtils.getField(
                        result.get(1), "remainingAmount"
                )
        );

        assertEquals(
                false,
                ReflectionTestUtils.getField(
                        result.get(0), "exceeded"
                )
        );

        assertEquals(
                true,
                ReflectionTestUtils.getField(
                        result.get(1), "exceeded"
                )
        );
    }

    @Test
    void getMonthlyBudgetStatus_shouldReturnEmptyListWithoutBudgets() {
        mockLoggedInUser();

        when(budgetRepository.findByUserIdAndYearAndMonth(
                USER_ID, YEAR, MONTH
        )).thenReturn(List.of());

        List<BudgetStatusResponse> result =
                dashboardService.getMonthlyBudgetStatus(YEAR, MONTH);

        assertTrue(result.isEmpty());

        verifyNoInteractions(transactionRepository);
    }

    // UPCOMING SUBSCRIPTIONS

    @Test
    void getUpcomingSubscriptions_shouldQueryNext30Days() {
        mockLoggedInUser();

        List<Subscription> expected =
                List.of(mock(Subscription.class));

        when(subscriptionRepository
                .findByUserIdAndStatusAndNextBillingDateBetweenOrderByNextBillingDateAsc(
                        eq(USER_ID),
                        eq(BillingStatus.ACTIVE),
                        any(LocalDate.class),
                        any(LocalDate.class)
                )).thenReturn(expected);

        LocalDate before = LocalDate.now();

        List<Subscription> result =
                dashboardService.getUpcomingSubscriptions();

        LocalDate after = LocalDate.now();

        assertSame(expected, result);

        ArgumentCaptor<LocalDate> fromCaptor =
                ArgumentCaptor.forClass(LocalDate.class);

        ArgumentCaptor<LocalDate> toCaptor =
                ArgumentCaptor.forClass(LocalDate.class);

        verify(subscriptionRepository)
                .findByUserIdAndStatusAndNextBillingDateBetweenOrderByNextBillingDateAsc(
                        eq(USER_ID),
                        eq(BillingStatus.ACTIVE),
                        fromCaptor.capture(),
                        toCaptor.capture()
                );

        LocalDate from = fromCaptor.getValue();
        LocalDate to = toCaptor.getValue();

        assertFalse(from.isBefore(before));
        assertFalse(from.isAfter(after));

        assertEquals(from.plusDays(30), to);
    }

    @Test
    void getUpcomingSubscriptions_shouldReturnEmptyListWhenNoneFound() {
        mockLoggedInUser();

        when(subscriptionRepository
                .findByUserIdAndStatusAndNextBillingDateBetweenOrderByNextBillingDateAsc(
                        eq(USER_ID),
                        eq(BillingStatus.ACTIVE),
                        any(LocalDate.class),
                        any(LocalDate.class)
                )).thenReturn(List.of());

        List<Subscription> result =
                dashboardService.getUpcomingSubscriptions();

        assertTrue(result.isEmpty());
    }
}
