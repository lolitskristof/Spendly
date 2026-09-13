package dev.kristof.spendly.dashboard;

import dev.kristof.spendly.dashboard.dto.CategoryExpenseSummary;
import dev.kristof.spendly.dashboard.dto.MonthlySummary;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/monthly-summary")
    public MonthlySummary getMonthlySummary(
            @RequestParam
            @Min(value = 2000, message = "Year must be at least 2000")
            Integer year,

            @RequestParam
            @Min(value = 1, message = "Month must be between 1 and 12")
            @Max(value = 12, message = "Month must be between 1 and 12")
            Integer month
    ) {
        return dashboardService.getMonthlySummary(year, month);
    }

    @GetMapping("/category-expenses")
    public List<CategoryExpenseSummary> getCategoryExpenseSummary(
            @RequestParam
            @Min(value = 2000, message = "Year must be at least 2000")
            Integer year,

            @RequestParam
            @Min(value = 1, message = "Month must be between 1 and 12")
            @Max(value = 12, message = "Month must be between 1 and 12")
            Integer month
    ) {
        return dashboardService.getCategoryExpenseSummary(year, month);
    }
}