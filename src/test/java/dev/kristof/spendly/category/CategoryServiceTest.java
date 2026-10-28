package dev.kristof.spendly.category;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.common.exception.CategoryAlreadyExists;
import dev.kristof.spendly.common.exception.CategoryNotFoundException;
import dev.kristof.spendly.user.User;
import dev.kristof.spendly.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    private static final Long USER_ID = 42L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long CATEGORY_ID = 10L;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private User mockLoggedInUser() {
        User user = mock(User.class);
        when(currentUserService.getLoggedInUser()).thenReturn(user);
        return user;
    }

    private User mockLoggedInUserWithId() {
        User user = mockLoggedInUser();
        when(user.getId()).thenReturn(USER_ID);
        return user;
    }

    private User mockRegularUser() {
        User user = mockLoggedInUserWithId();
        when(user.getRole()).thenReturn(UserRole.USER);
        return user;
    }

    private Category mockOwnedCategory(Long ownerId) {
        Category category = mock(Category.class);
        User owner = mock(User.class);

        when(category.getUser()).thenReturn(owner);
        when(owner.getId()).thenReturn(ownerId);

        return category;
    }

    private void mockExistingCategory(Category category) {
        when(categoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.of(category));
    }

    // LIST

    @Test
    void getCategoriesForUser_shouldReturnAccessibleCategories() {
        mockLoggedInUserWithId();

        List<Category> expected = List.of(
                mock(Category.class),
                mock(Category.class)
        );

        when(categoryRepository.findByUserIsNullOrUserId(USER_ID))
                .thenReturn(expected);

        List<Category> result =
                categoryService.getCategoriesForUser();

        assertSame(expected, result);

        verify(categoryRepository)
                .findByUserIsNullOrUserId(USER_ID);
    }

    @Test
    void getCategoriesForUser_shouldReturnEmptyList() {
        mockLoggedInUserWithId();

        when(categoryRepository.findByUserIsNullOrUserId(USER_ID))
                .thenReturn(List.of());

        List<Category> result =
                categoryService.getCategoriesForUser();

        assertTrue(result.isEmpty());
    }

    // CREATE

    @Test
    void createCategory_shouldSaveCategoryForLoggedInUser() {
        User user = mockLoggedInUserWithId();

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Category result = categoryService.createCategory(
                "Food",
                CategoryType.EXPENSE
        );

        assertNotNull(result);
        assertEquals("Food", result.getName());
        assertEquals(CategoryType.EXPENSE, result.getCategoryType());
        assertSame(user, result.getUser());

        verify(categoryRepository)
                .existsByUserIdAndNameIgnoreCaseAndCategoryType(
                        USER_ID,
                        "Food",
                        CategoryType.EXPENSE
                );

        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_shouldRejectDuplicateCategory() {
        mockLoggedInUserWithId();

        when(categoryRepository
                .existsByUserIdAndNameIgnoreCaseAndCategoryType(
                        USER_ID,
                        "Food",
                        CategoryType.EXPENSE
                )).thenReturn(true);

        assertThrows(
                CategoryAlreadyExists.class,
                () -> categoryService.createCategory(
                        "Food",
                        CategoryType.EXPENSE
                )
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    // UPDATE

    @Test
    void updateCategory_shouldAllowOwner() {
        mockRegularUser();

        Category category = mockOwnedCategory(USER_ID);
        mockExistingCategory(category);

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result = categoryService.updateCategory(
                CATEGORY_ID,
                "Updated Food",
                CategoryType.EXPENSE
        );

        assertSame(category, result);

        verify(categoryRepository)
                .existsByUserIdAndNameIgnoreCaseAndCategoryTypeAndIdNot(
                        USER_ID,
                        "Updated Food",
                        CategoryType.EXPENSE,
                        CATEGORY_ID
                );

        verify(category).setName("Updated Food");
        verify(category).setCategoryType(CategoryType.EXPENSE);
        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_shouldKeepExistingValuesWhenNull() {
        mockRegularUser();

        Category category = mockOwnedCategory(USER_ID);
        mockExistingCategory(category);

        when(category.getName()).thenReturn("Food");
        when(category.getCategoryType())
                .thenReturn(CategoryType.EXPENSE);

        when(categoryRepository.save(category))
                .thenReturn(category);

        Category result = categoryService.updateCategory(
                CATEGORY_ID,
                null,
                null
        );

        assertSame(category, result);

        verify(category).setName("Food");
        verify(category).setCategoryType(CategoryType.EXPENSE);

        verify(categoryRepository)
                .existsByUserIdAndNameIgnoreCaseAndCategoryTypeAndIdNot(
                        USER_ID,
                        "Food",
                        CategoryType.EXPENSE,
                        CATEGORY_ID
                );
    }

    @Test
    void updateCategory_shouldAllowAdminToUpdateSystemCategory() {
        User admin = mockLoggedInUser();

        when(admin.getRole()).thenReturn(UserRole.ADMIN);

        Category systemCategory = mock(Category.class);
        mockExistingCategory(systemCategory);

        when(categoryRepository.save(systemCategory))
                .thenReturn(systemCategory);

        Category result = categoryService.updateCategory(
                CATEGORY_ID,
                "Housing",
                CategoryType.EXPENSE
        );

        assertSame(systemCategory, result);

        verify(categoryRepository)
                .existsByUserIsNullAndNameIgnoreCaseAndCategoryTypeAndIdNot(
                        "Housing",
                        CategoryType.EXPENSE,
                        CATEGORY_ID
                );

        verify(systemCategory).setName("Housing");
        verify(systemCategory).setCategoryType(CategoryType.EXPENSE);
        verify(categoryRepository).save(systemCategory);
    }

    @Test
    void updateCategory_shouldAllowAdminToUpdateOtherUsersCategory() {
        User admin = mockLoggedInUserWithId();

        when(admin.getRole()).thenReturn(UserRole.ADMIN);

        Category category = mockOwnedCategory(OTHER_USER_ID);
        mockExistingCategory(category);

        when(categoryRepository.save(category))
                .thenReturn(category);

        categoryService.updateCategory(
                CATEGORY_ID,
                "Updated",
                CategoryType.EXPENSE
        );

        verify(categoryRepository)
                .existsByUserIdAndNameIgnoreCaseAndCategoryTypeAndIdNot(
                        OTHER_USER_ID,
                        "Updated",
                        CategoryType.EXPENSE,
                        CATEGORY_ID
                );

        verify(categoryRepository).save(category);
    }

    @Test
    void updateCategory_shouldRejectSystemCategoryForRegularUser() {
        User user = mockLoggedInUser();

        when(user.getRole()).thenReturn(UserRole.USER);

        Category systemCategory = mock(Category.class);
        mockExistingCategory(systemCategory);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.updateCategory(
                        CATEGORY_ID,
                        "Updated",
                        CategoryType.EXPENSE
                )
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void updateCategory_shouldRejectOtherUsersCategory() {
        mockRegularUser();

        Category category = mockOwnedCategory(OTHER_USER_ID);
        mockExistingCategory(category);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.updateCategory(
                        CATEGORY_ID,
                        "Updated",
                        CategoryType.EXPENSE
                )
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void updateCategory_shouldRejectMissingCategory() {
        mockLoggedInUser();

        when(categoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.updateCategory(
                        CATEGORY_ID,
                        "Updated",
                        CategoryType.EXPENSE
                )
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void updateCategory_shouldRejectDuplicateOwnedCategory() {
        mockRegularUser();

        Category category = mockOwnedCategory(USER_ID);
        mockExistingCategory(category);

        when(categoryRepository
                .existsByUserIdAndNameIgnoreCaseAndCategoryTypeAndIdNot(
                        USER_ID,
                        "Food",
                        CategoryType.EXPENSE,
                        CATEGORY_ID
                )).thenReturn(true);

        assertThrows(
                CategoryAlreadyExists.class,
                () -> categoryService.updateCategory(
                        CATEGORY_ID,
                        "Food",
                        CategoryType.EXPENSE
                )
        );

        verify(category, never()).setName(anyString());
        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    @Test
    void updateCategory_shouldRejectDuplicateSystemCategory() {
        User admin = mockLoggedInUser();

        when(admin.getRole()).thenReturn(UserRole.ADMIN);

        Category systemCategory = mock(Category.class);
        mockExistingCategory(systemCategory);

        when(categoryRepository
                .existsByUserIsNullAndNameIgnoreCaseAndCategoryTypeAndIdNot(
                        "Food",
                        CategoryType.EXPENSE,
                        CATEGORY_ID
                )).thenReturn(true);

        assertThrows(
                CategoryAlreadyExists.class,
                () -> categoryService.updateCategory(
                        CATEGORY_ID,
                        "Food",
                        CategoryType.EXPENSE
                )
        );

        verify(categoryRepository, never())
                .save(any(Category.class));
    }

    // DELETE

    @Test
    void deleteCategory_shouldAllowOwner() {
        mockRegularUser();

        Category category = mockOwnedCategory(USER_ID);
        mockExistingCategory(category);

        categoryService.deleteCategory(CATEGORY_ID);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldAllowAdminToDeleteSystemCategory() {
        User admin = mockLoggedInUser();

        when(admin.getRole()).thenReturn(UserRole.ADMIN);

        Category systemCategory = mock(Category.class);
        mockExistingCategory(systemCategory);

        categoryService.deleteCategory(CATEGORY_ID);

        verify(categoryRepository).delete(systemCategory);
    }

    @Test
    void deleteCategory_shouldAllowAdminToDeleteOtherUsersCategory() {
        User admin = mockLoggedInUserWithId();

        when(admin.getRole()).thenReturn(UserRole.ADMIN);

        Category category = mockOwnedCategory(OTHER_USER_ID);
        mockExistingCategory(category);

        categoryService.deleteCategory(CATEGORY_ID);

        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldRejectSystemCategoryForRegularUser() {
        User user = mockLoggedInUser();

        when(user.getRole()).thenReturn(UserRole.USER);

        Category systemCategory = mock(Category.class);
        mockExistingCategory(systemCategory);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }

    @Test
    void deleteCategory_shouldRejectOtherUsersCategory() {
        mockRegularUser();

        Category category = mockOwnedCategory(OTHER_USER_ID);
        mockExistingCategory(category);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }

    @Test
    void deleteCategory_shouldRejectMissingCategory() {
        mockLoggedInUser();

        when(categoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(CATEGORY_ID)
        );

        verify(categoryRepository, never())
                .delete(any(Category.class));
    }

    // ACCESSIBLE CATEGORY

    @Test
    void getAccessibleCategoryById_shouldAllowSystemCategory() {
        mockLoggedInUser();

        Category systemCategory = mock(Category.class);
        mockExistingCategory(systemCategory);

        Category result =
                categoryService.getAccessibleCategoryById(CATEGORY_ID);

        assertSame(systemCategory, result);
    }

    @Test
    void getAccessibleCategoryById_shouldAllowOwnedCategory() {
        mockLoggedInUserWithId();

        Category category = mockOwnedCategory(USER_ID);
        mockExistingCategory(category);

        Category result =
                categoryService.getAccessibleCategoryById(CATEGORY_ID);

        assertSame(category, result);
    }

    @Test
    void getAccessibleCategoryById_shouldRejectOtherUsersCategory() {
        mockLoggedInUserWithId();

        Category category = mockOwnedCategory(OTHER_USER_ID);
        mockExistingCategory(category);

        assertThrows(
                AccessDeniedException.class,
                () -> categoryService.getAccessibleCategoryById(CATEGORY_ID)
        );
    }

    @Test
    void getAccessibleCategoryById_shouldRejectMissingCategory() {
        mockLoggedInUser();

        when(categoryRepository.findById(CATEGORY_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getAccessibleCategoryById(CATEGORY_ID)
        );
    }
}
