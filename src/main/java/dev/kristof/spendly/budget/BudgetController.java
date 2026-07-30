package dev.kristof.spendly.budget;

import dev.kristof.spendly.budget.dto.BudgetResponse;
import dev.kristof.spendly.budget.dto.CreateBudgetRequest;
import dev.kristof.spendly.budget.dto.UpdateBudgetRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/budget")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public BudgetResponse createBudget(@Valid @RequestBody CreateBudgetRequest request) {
        Budget budget = budgetService.createBudget(request.getCategoryId(), request.getAmount(), request.getYear(), request.getMonth());

        return new BudgetResponse(budget.getId(), budget.getUser().getId(), budget.getCategory().getId(), budget.getAmount(), budget.getYear(), budget.getMonth());
    }

    @GetMapping
    public List<BudgetResponse> getBudgets(
            @RequestParam(required = false)
            @Min(value = 2000, message = "Year must be at least 2000")
            Integer year,

            @RequestParam(required = false)
            @Min(value = 1, message = "Month must be between 1 and 12")
            @Max(value = 12, message = "Month must be between 1 and 12")
            Integer month
    ) {
        List<Budget> budgets = budgetService.getOwnedBudgets(year, month);

        return budgets.stream().map(budget -> new BudgetResponse(
                budget.getId(),
                budget.getUser().getId(),
                budget.getCategory().getId(),
                budget.getAmount(),
                budget.getYear(),
                budget.getMonth()
        )).toList();
    }

    @PatchMapping("/{budgetId}")
    public BudgetResponse updateBudget(@PathVariable Long budgetId, @Valid @RequestBody UpdateBudgetRequest request) {
        Budget budget = budgetService.updateBudget(budgetId, request.getCategoryId(), request.getAmount(), request.getYear(), request.getMonth());

        return new BudgetResponse(budget.getId(), budget.getUser().getId(), budget.getCategory().getId(), budget.getAmount(), budget.getYear(), budget.getMonth());
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long budgetId) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.noContent().build();
    }
}
