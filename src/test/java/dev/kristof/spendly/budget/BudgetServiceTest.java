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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    private static final Long USER_ID = 42L;
    private static final Long BUDGET_ID = 10L;
    private static final Long CATEGORY_ID = 3L;
    private static final Long NEW_CATEGORY_ID = 4L;

    private static final Integer YEAR = 2026;
    private static final Integer MONTH = 7;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private BudgetService budgetService;

    private User mockLoggedInUser() {
        User user = mock(User.class);

        when(currentUserService.getLoggedInUser())
                .thenReturn(user);

        return user;
    }

    private User mockLoggedInUserWithId() {
        User user = mockLoggedInUser();

        when(user.getId()).thenReturn(USER_ID);

        return user;
    }

    private Budget mockOwnedBudget() {
        mockLoggedInUserWithId();

        Budget budget = mock(Budget.class);

        when(budgetRepository.findByIdAndUserId(BUDGET_ID, USER_ID))
                .thenReturn(Optional.of(budget));

        return budget;
    }

    // CREATE

    @Test
    void createBudget_shouldSaveValidExpenseBudget() {
        User user = mockLoggedInUserWithId();
        Category category = mock(Category.class);

        BigDecimal amount = new BigDecimal("100000.00");

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenReturn(category);

        when(category.getCategoryType())
                .thenReturn(CategoryType.EXPENSE);

        when(category.getId()).thenReturn(CATEGORY_ID);

        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Budget result = budgetService.createBudget(
                CATEGORY_ID, amount, YEAR, MONTH
        );

        ArgumentCaptor<Budget> captor =
                ArgumentCaptor.forClass(Budget.class);

        verify(budgetRepository).save(captor.capture());

        Budget savedBudget = captor.getValue();

        assertSame(savedBudget, result);
        assertSame(category, savedBudget.getCategory());
        assertEquals(amount, savedBudget.getAmount());
        assertEquals(YEAR, savedBudget.getYear());
        assertEquals(MONTH, savedBudget.getMonth());

        assertSame(
                user,
                ReflectionTestUtils.getField(savedBudget, "user")
        );

        verify(budgetRepository)
                .existsByUserIdAndCategoryIdAndYearAndMonth(
                        USER_ID, CATEGORY_ID, YEAR, MONTH
                );
    }

    @Test
    void createBudget_shouldRejectIncomeCategory() {
        mockLoggedInUser();

        Category category = mock(Category.class);

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenReturn(category);

        when(category.getCategoryType())
                .thenReturn(CategoryType.INCOME);

        assertThrows(
                InvalidBudgetCategoryException.class,
                () -> budgetService.createBudget(
                        CATEGORY_ID,
                        new BigDecimal("100000.00"),
                        YEAR,
                        MONTH
                )
        );

        verifyNoInteractions(budgetRepository);
    }

    @Test
    void createBudget_shouldRejectDuplicateBudget() {
        mockLoggedInUserWithId();

        Category category = mock(Category.class);

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenReturn(category);

        when(category.getCategoryType())
                .thenReturn(CategoryType.EXPENSE);

        when(category.getId()).thenReturn(CATEGORY_ID);

        when(budgetRepository
                .existsByUserIdAndCategoryIdAndYearAndMonth(
                        USER_ID, CATEGORY_ID, YEAR, MONTH
                )).thenReturn(true);

        assertThrows(
                BudgetAlreadyExistsException.class,
                () -> budgetService.createBudget(
                        CATEGORY_ID,
                        new BigDecimal("100000.00"),
                        YEAR,
                        MONTH
                )
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void createBudget_shouldRejectInaccessibleCategory() {
        mockLoggedInUser();

        when(categoryService.getAccessibleCategoryById(CATEGORY_ID))
                .thenThrow(new IllegalArgumentException(
                        "Category not accessible"
                ));

        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.createBudget(
                        CATEGORY_ID,
                        BigDecimal.TEN,
                        YEAR,
                        MONTH
                )
        );

        verifyNoInteractions(budgetRepository);
    }

    // LIST

    @Test
    void getOwnedBudgets_shouldReturnAllOwnedBudgets() {
        mockLoggedInUserWithId();

        List<Budget> expected = List.of(mock(Budget.class));

        when(budgetRepository.findByUserId(USER_ID))
                .thenReturn(expected);

        List<Budget> result =
                budgetService.getOwnedBudgets(null, null);

        assertSame(expected, result);

        verify(budgetRepository).findByUserId(USER_ID);
    }

    @Test
    void getOwnedBudgets_shouldFilterByYearAndMonth() {
        mockLoggedInUserWithId();

        List<Budget> expected = List.of(mock(Budget.class));

        when(budgetRepository.findByUserIdAndYearAndMonth(
                USER_ID, YEAR, MONTH
        )).thenReturn(expected);

        List<Budget> result =
                budgetService.getOwnedBudgets(YEAR, MONTH);

        assertSame(expected, result);

        verify(budgetRepository).findByUserIdAndYearAndMonth(
                USER_ID, YEAR, MONTH
        );
    }

    @Test
    void getOwnedBudgets_shouldRejectYearWithoutMonth() {
        mockLoggedInUser();

        assertThrows(
                InvalidBudgetPeriodException.class,
                () -> budgetService.getOwnedBudgets(YEAR, null)
        );

        verifyNoInteractions(budgetRepository);
    }

    @Test
    void getOwnedBudgets_shouldRejectMonthWithoutYear() {
        mockLoggedInUser();

        assertThrows(
                InvalidBudgetPeriodException.class,
                () -> budgetService.getOwnedBudgets(null, MONTH)
        );

        verifyNoInteractions(budgetRepository);
    }

    // UPDATE

    @Test
    void updateBudget_shouldUpdateAllFields() {
        Budget budget = mockOwnedBudget();
        Category newCategory = mock(Category.class);

        BigDecimal newAmount = new BigDecimal("150000.00");

        when(categoryService.getAccessibleCategoryById(NEW_CATEGORY_ID))
                .thenReturn(newCategory);

        when(newCategory.getCategoryType())
                .thenReturn(CategoryType.EXPENSE);

        when(newCategory.getId())
                .thenReturn(NEW_CATEGORY_ID);

        when(budgetRepository.save(budget))
                .thenReturn(budget);

        Budget result = budgetService.updateBudget(
                BUDGET_ID,
                NEW_CATEGORY_ID,
                newAmount,
                2027,
                8
        );

        assertSame(budget, result);

        verify(budget).setCategory(newCategory);
        verify(budget).setAmount(newAmount);
        verify(budget).setYear(2027);
        verify(budget).setMonth(8);

        verify(budgetRepository)
                .existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                        USER_ID,
                        NEW_CATEGORY_ID,
                        2027,
                        8,
                        BUDGET_ID
                );

        verify(budgetRepository).save(budget);
    }

    @Test
    void updateBudget_shouldKeepExistingValuesWhenNull() {
        Budget budget = mockOwnedBudget();
        Category originalCategory = mock(Category.class);

        BigDecimal originalAmount =
                new BigDecimal("100000.00");

        when(budget.getCategory())
                .thenReturn(originalCategory);

        when(originalCategory.getId())
                .thenReturn(CATEGORY_ID);

        when(budget.getAmount())
                .thenReturn(originalAmount);

        when(budget.getYear()).thenReturn(YEAR);
        when(budget.getMonth()).thenReturn(MONTH);

        when(budgetRepository.save(budget))
                .thenReturn(budget);

        Budget result = budgetService.updateBudget(
                BUDGET_ID,
                null,
                null,
                null,
                null
        );

        assertSame(budget, result);

        verify(budget).setCategory(originalCategory);
        verify(budget).setAmount(originalAmount);
        verify(budget).setYear(YEAR);
        verify(budget).setMonth(MONTH);

        verifyNoInteractions(categoryService);

        verify(budgetRepository)
                .existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                        USER_ID,
                        CATEGORY_ID,
                        YEAR,
                        MONTH,
                        BUDGET_ID
                );

        verify(budgetRepository).save(budget);
    }

    @Test
    void updateBudget_shouldRejectIncomeCategory() {
        mockOwnedBudget();

        Category newCategory = mock(Category.class);

        when(categoryService.getAccessibleCategoryById(NEW_CATEGORY_ID))
                .thenReturn(newCategory);

        when(newCategory.getCategoryType())
                .thenReturn(CategoryType.INCOME);

        assertThrows(
                InvalidBudgetCategoryException.class,
                () -> budgetService.updateBudget(
                        BUDGET_ID,
                        NEW_CATEGORY_ID,
                        null,
                        null,
                        null
                )
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_shouldRejectDuplicateBudget() {
        mockOwnedBudget();

        Category newCategory = mock(Category.class);

        when(categoryService.getAccessibleCategoryById(NEW_CATEGORY_ID))
                .thenReturn(newCategory);

        when(newCategory.getCategoryType())
                .thenReturn(CategoryType.EXPENSE);

        when(newCategory.getId())
                .thenReturn(NEW_CATEGORY_ID);

        when(budgetRepository
                .existsByUserIdAndCategoryIdAndYearAndMonthAndIdNot(
                        USER_ID,
                        NEW_CATEGORY_ID,
                        YEAR,
                        MONTH,
                        BUDGET_ID
                )).thenReturn(true);

        assertThrows(
                BudgetAlreadyExistsException.class,
                () -> budgetService.updateBudget(
                        BUDGET_ID,
                        NEW_CATEGORY_ID,
                        BigDecimal.TEN,
                        YEAR,
                        MONTH
                )
        );

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void updateBudget_shouldRejectBudgetNotOwnedByUser() {
        mockLoggedInUserWithId();

        when(budgetRepository.findByIdAndUserId(
                BUDGET_ID, USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.updateBudget(
                        BUDGET_ID,
                        NEW_CATEGORY_ID,
                        BigDecimal.TEN,
                        YEAR,
                        MONTH
                )
        );

        verifyNoInteractions(categoryService);

        verify(budgetRepository, never()).save(any(Budget.class));
    }

    // DELETE

    @Test
    void deleteBudget_shouldDeleteOwnedBudget() {
        Budget budget = mockOwnedBudget();

        budgetService.deleteBudget(BUDGET_ID);

        verify(budgetRepository).delete(budget);
    }

    @Test
    void deleteBudget_shouldRejectBudgetNotOwnedByUser() {
        mockLoggedInUserWithId();

        when(budgetRepository.findByIdAndUserId(
                BUDGET_ID, USER_ID
        )).thenReturn(Optional.empty());

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.deleteBudget(BUDGET_ID)
        );

        verify(budgetRepository, never())
                .delete(any(Budget.class));
    }
}
