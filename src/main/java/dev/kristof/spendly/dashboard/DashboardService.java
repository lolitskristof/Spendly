package dev.kristof.spendly.dashboard;


import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.CategoryType;
import dev.kristof.spendly.dashboard.dto.CategoryExpenseSummary;
import dev.kristof.spendly.dashboard.dto.MonthlySummary;
import dev.kristof.spendly.transaction.TransactionRepository;
import dev.kristof.spendly.user.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class DashboardService {
    private final CurrentUserService currentUserService;
    private final TransactionRepository transactionRepository;

    public DashboardService(CurrentUserService currentUserService, TransactionRepository transactionRepository) {
        this.currentUserService = currentUserService;
        this.transactionRepository = transactionRepository;
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
}
