package dev.kristof.spendly.category;

import dev.kristof.spendly.category.dto.CategoryResponse;
import dev.kristof.spendly.category.dto.CreateCategoryRequest;
import dev.kristof.spendly.category.dto.UpdateCategoryRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping()
    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryService.getCategoriesForUser();
        return categories.stream().map(category -> new CategoryResponse(
                category.getId(),
                category.getUser() != null
                        ? category.getUser().getId()
                        : null,
                category.getCategoryType(),
                category.getName()
        )).toList();
    }

    @PostMapping()
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest createCategoryRequest) {
        Category category = categoryService.createCategory(createCategoryRequest.getName(), createCategoryRequest.getCategoryType());

        return new CategoryResponse(category.getId(), category.getUser().getId(), category.getCategoryType(), category.getName());
    }

    @PatchMapping("/{categoryId}")
    public CategoryResponse updateCategory(@PathVariable Long categoryId, @Valid @RequestBody UpdateCategoryRequest updateCategoryRequest) {
        Category category = categoryService.updateCategory(categoryId, updateCategoryRequest.getName(), updateCategoryRequest.getCategoryType());
        Long userId = category.getUser() != null
                ? category.getUser().getId()
                : null;
        return new CategoryResponse(category.getId(), userId, category.getCategoryType(), category.getName());
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

}
