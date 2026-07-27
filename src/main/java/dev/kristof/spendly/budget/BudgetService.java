package dev.kristof.spendly.budget;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class BudgetService {
    private final CurrentUserService currentUserService;
    private final BudgetRepository budgetRepository;
    private final CategoryService categoryService;

    public BudgetService(CurrentUserService currentUserService, BudgetRepository budgetRepository, CategoryService categoryService) {
        this.currentUserService = currentUserService;
        this.budgetRepository = budgetRepository;
        this.categoryService = categoryService;
    }
}
