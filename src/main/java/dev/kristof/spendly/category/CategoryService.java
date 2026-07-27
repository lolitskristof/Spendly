package dev.kristof.spendly.category;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.common.exception.CategoryAlreadyExists;
import dev.kristof.spendly.common.exception.CategoryNotFoundException;
import dev.kristof.spendly.user.User;
import dev.kristof.spendly.user.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CurrentUserService currentUserService;
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository, CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    public List<Category> getCategoriesForUser() {
        User loggedInUser = currentUserService.getLoggedInUser();
        return categoryRepository.findByUserIsNullOrUserId(loggedInUser.getId());
    }

    public Category createCategory(String name, CategoryType categoryType) {
        User loggedInUser = currentUserService.getLoggedInUser();
        boolean alreadyExists = categoryRepository.existsByUserIdAndNameIgnoreCaseAndCategoryType(loggedInUser.getId(), name, categoryType);
        if (alreadyExists) {
            throw new CategoryAlreadyExists("Category " + name + " already exists");
        }
        return categoryRepository.save(new Category(name, categoryType, loggedInUser));
    }

    public Category updateCategory(Long categoryId, String name, CategoryType categoryType) {
        User loggedInUser = currentUserService.getLoggedInUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id " + categoryId
                        )
                );

        boolean isAdmin = loggedInUser.getRole() == UserRole.ADMIN;
        boolean isSystemCategory = category.getUser() == null;
        boolean isOwner = !isSystemCategory
                && category.getUser().getId().equals(loggedInUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You are not allowed to update this category"
            );
        }

        String updatedName = name != null ? name : category.getName();
        CategoryType updatedType = categoryType != null
                ? categoryType
                : category.getCategoryType();

        boolean alreadyExists;

        if (isSystemCategory) {
            alreadyExists =
                    categoryRepository
                            .existsByUserIsNullAndNameIgnoreCaseAndCategoryTypeAndIdNot(
                                    updatedName,
                                    updatedType,
                                    categoryId
                            );
        } else {
            alreadyExists =
                    categoryRepository
                            .existsByUserIdAndNameIgnoreCaseAndCategoryTypeAndIdNot(
                                    category.getUser().getId(),
                                    updatedName,
                                    updatedType,
                                    categoryId
                            );
        }

        if (alreadyExists) {
            throw new CategoryAlreadyExists(
                    "Category " + updatedName + " already exists"
            );
        }

        category.setName(updatedName);
        category.setCategoryType(updatedType);

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long categoryId) {
        User loggedInUser = currentUserService.getLoggedInUser();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id " + categoryId
                        )
                );

        boolean isAdmin = loggedInUser.getRole() == UserRole.ADMIN;
        boolean isSystemCategory = category.getUser() == null;
        boolean isOwner = !isSystemCategory
                && category.getUser().getId().equals(loggedInUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(
                    "You are not allowed to delete this category"
            );
        }

        categoryRepository.delete(category);
    }

    public Category getAccessibleCategoryById(Long categoryId) {
        User loggedInUser = currentUserService.getLoggedInUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                "Category not found with id " + categoryId
                        )
                );

        boolean isSystemCategory = category.getUser() == null;
        boolean isOwner = !isSystemCategory
                && category.getUser().getId().equals(loggedInUser.getId());

        if (!isSystemCategory && !isOwner) {
            throw new AccessDeniedException(
                    "You are not allowed to use this category"
            );
        }

        return category;
    }
}
