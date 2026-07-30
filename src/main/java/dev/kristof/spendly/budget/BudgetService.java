package dev.kristof.spendly.budget;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.category.Category;
import dev.kristof.spendly.category.CategoryService;
import dev.kristof.spendly.category.CategoryType;
import dev.kristof.spendly.common.exception.BudgetAlreadyExistsException;
import dev.kristof.spendly.common.exception.BudgetNotFoundException;
import dev.kristof.spendly.common.exception.InvalidBudgetCategoryException;
import dev.kristof.spendly.common.exception.InvalidBudgetPeriodException;
import dev.kristof.spendly.user.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

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

    public Budget createBudget(Long categoryId, BigDecimal amount, Integer year, Integer month) {
        User currentUser = currentUserService.getLoggedInUser();
        Category userCategory = categoryService.getAccessibleCategoryById(categoryId);

        if (userCategory.getCategoryType() != CategoryType.EXPENSE) {
            throw new InvalidBudgetCategoryException("Budget can only be created for expense categories");
        }

        boolean budgetAlreadyExists = budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonth(currentUser.getId(), userCategory.getId(), year, month);

        if (budgetAlreadyExists) {
            throw new BudgetAlreadyExistsException("Budget already exists");
        }

        Budget budget = new Budget(currentUser, userCategory, amount, year, month);
        return budgetRepository.save(budget);
    }

    public List<Budget> getOwnedBudgets(Integer year, Integer month) {
        User currentUser = currentUserService.getLoggedInUser();
        boolean hasYear = year != null;
        boolean hasMonth = month != null;

        if (hasYear && hasMonth) {
            return budgetRepository.findByUserIdAndYearAndMonth(currentUser.getId(), year, month);
        }

        if (hasYear != hasMonth) {
            throw new InvalidBudgetPeriodException("Year and month must be provided together");
        }

        return budgetRepository.findByUserId(currentUser.getId());
    }

    public Budget updateBudget(Long budgetId, Long categoryId, BigDecimal amount, Integer year, Integer month) {
        User currentUser = currentUserService.getLoggedInUser();
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, currentUser.getId()).orElseThrow(() ->
                new BudgetNotFoundException("Budget not found with id: " + budgetId)
        );

        Category updatedCategory = budget.getCategory();

        if (categoryId != null) {
            updatedCategory = categoryService.getAccessibleCategoryById(categoryId);

            if (updatedCategory.getCategoryType() != CategoryType.EXPENSE) {
                throw new InvalidBudgetCategoryException(
                        "Budget can only be updated for expense categories"
                );
            }
        }

        BigDecimal updatedAmount = amount != null ? amount : budget.getAmount();
        Integer updatedYear = year != null ? year : budget.getYear();
        Integer updatedMonth = month != null ? month : budget.getMonth();

        boolean alreadyExists =
                budgetRepository.existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                        currentUser.getId(),
                        updatedCategory.getId(),
                        updatedYear,
                        updatedMonth,
                        budgetId
                );

        if (alreadyExists) {
            throw new BudgetAlreadyExistsException(
                    "Budget already exists for this category and period"
            );
        }

        budget.setCategory(updatedCategory);
        budget.setAmount(updatedAmount);
        budget.setYear(updatedYear);
        budget.setMonth(updatedMonth);

        return budgetRepository.save(budget);
    }

    public void deleteBudget(Long budgetId) {
        User currentUser = currentUserService.getLoggedInUser();
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, currentUser.getId()).orElseThrow(() ->
                new BudgetNotFoundException("Budget not found with id: " + budgetId)
        );
        budgetRepository.delete(budget);
    }
}
