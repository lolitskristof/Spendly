package dev.kristof.spendly.report;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.CategoryType;
import dev.kristof.spendly.report.dto.DateRangeSummaryResponse;
import dev.kristof.spendly.transaction.Transaction;
import dev.kristof.spendly.transaction.TransactionRepository;
import dev.kristof.spendly.user.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class ReportService {
    private final CurrentUserService currentUserService;
    private final TransactionRepository transactionRepository;

    public ReportService(CurrentUserService currentUserService, TransactionRepository transactionRepository) {
        this.currentUserService = currentUserService;
        this.transactionRepository = transactionRepository;
    }

    public DateRangeSummaryResponse getDateRangeSummary(OffsetDateTime from, OffsetDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }

        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }


        User user = currentUserService.getLoggedInUser();
        BigDecimal income = transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(user.getId(), from, to, CategoryType.INCOME);
        BigDecimal expense = transactionRepository.sumAmountByUserAndDateRangeAndCategoryType(user.getId(), from, to, CategoryType.EXPENSE);

        return new DateRangeSummaryResponse(
                income,
                expense,
                income.subtract(expense)
        );
    }

    public DateRangeSummaryResponse getMonthlyReport(Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);

        OffsetDateTime from = yearMonth
                .atDay(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime to = yearMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC)
                .minusNanos(1_000);

        return getDateRangeSummary(from, to);
    }

    public String exportTransactionsToCsv(OffsetDateTime from, OffsetDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }

        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        User user = currentUserService.getLoggedInUser();

        List<Transaction> transactions = transactionRepository.findTransactionsForExport(user.getId(), from, to);

        StringBuilder csv = new StringBuilder();

        csv.append("Date,Type,Category,Amount,Description\n");

        for (Transaction t : transactions) {
            csv.append(t.getTransactionDate()).append(",");
            csv.append(t.getCategory().getCategoryType()).append(",");
            csv.append(escapeCsv(t.getCategory().getName())).append(",");
            csv.append(t.getAmount().toPlainString()).append(",");
            csv.append(escapeCsv(t.getDescription())).append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.stripLeading();

        if (!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0) {
            value = "'" + value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
