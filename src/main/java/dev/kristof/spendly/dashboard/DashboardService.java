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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Service
public class DashboardService {
    private final CurrentUserService currentUserService;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SubscriptionRepository subscriptionRepository;

    public DashboardService(CurrentUserService currentUserService, TransactionRepository transactionRepository, BudgetRepository budgetRepository, SubscriptionRepository subscriptionRepository) {
        this.currentUserService = currentUserService;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    public MonthlySummary getMonthlySummary(Integer year, Integer month) {
        User user = currentUserService.getLoggedInUser();

        YearMonth yearMonth = YearMonth.of(year, month);

        OffsetDateTime from = yearMonth
                .atDay(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime to = yearMonth
                .atEndOfMonth()
                .atTime(LocalTime.MAX)
                .atOffset(ZoneOffset.UTC);

        BigDecimal income = transactionRepository
                .sumAmountByUserAndDateRangeAndCategoryType(
                        user.getId(),
                        from,
                        to,
                        CategoryType.INCOME
                );

        BigDecimal expense = transactionRepository
                .sumAmountByUserAndDateRangeAndCategoryType(
                        user.getId(),
                        from,
                        to,
                        CategoryType.EXPENSE
                );

        BigDecimal monthlyBalance = income.subtract(expense);

        return new MonthlySummary(income, expense, monthlyBalance);
    }

    public List<CategoryExpenseSummary> getCategoryExpenseSummary(Integer year, Integer month) {
        User user = currentUserService.getLoggedInUser();

        YearMonth yearMonth = YearMonth.of(year, month);

        OffsetDateTime from = yearMonth
                .atDay(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime to = yearMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        return transactionRepository.getExpenseSummaryByCategory(
                user.getId(),
                from,
                to,
                CategoryType.EXPENSE
        );
    }

    @Transactional(readOnly = true)
    public List<BudgetStatusResponse> getMonthlyBudgetStatus(Integer year, Integer month) {
        User user = currentUserService.getLoggedInUser();

        YearMonth yearMonth = YearMonth.of(year, month);

        OffsetDateTime from = yearMonth
                .atDay(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime to = yearMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        List<Budget> budgets = budgetRepository.findByUserIdAndYearAndMonth(
                user.getId(), year, month
        );

        return budgets.stream().map(budget -> {
            Category category = budget.getCategory();

            BigDecimal spentAmount = transactionRepository.sumSpentByCategory(
                    user.getId(),
                    category.getId(),
                    from,
                    to
            );

            BigDecimal remainingAmount = budget.getAmount().subtract(spentAmount);

            boolean exceeded = spentAmount.compareTo(budget.getAmount()) > 0;

            return new BudgetStatusResponse(
                    budget.getId(),
                    category.getId(),
                    category.getName(),
                    budget.getAmount(),
                    spentAmount,
                    remainingAmount,
                    exceeded
            );
        }).toList();
    }

    public List<Subscription> getUpcomingSubscriptions() {
        User user = currentUserService.getLoggedInUser();
        LocalDate from = LocalDate.now();
        return subscriptionRepository.findByUserIdAndStatusAndNextBillingDateBetweenOrderByNextBillingDateAsc(user.getId(), BillingStatus.ACTIVE, from, from.plusDays(30));
    }
}
