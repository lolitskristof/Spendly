import { Component, inject, signal, computed } from '@angular/core';
import { CategoryService } from '../../services/category-service';
import { CategoryResponse } from '../../models/category-response';
import { ReactiveFormsModule, Validators, FormGroup, FormControl } from '@angular/forms';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-categories',
  styleUrl: './categories.css',
  templateUrl: './categories.html',
})
export class Categories {
  private categoryService = inject(CategoryService);
  categories = signal<CategoryResponse[]>([]);

  createError = signal<string | null>(null);
  editError = signal<string | null>(null);
  deleteError = signal<string | null>(null);

  showCreateForm = signal(false);

  defaultCategories = computed(() =>
    this.categories().filter((category) => category.userId === null),
  );

  myCategories = computed(() => this.categories().filter((category) => category.userId !== null));

  editingCategoryId = signal<number | null>(null);

  loadCategories() {
    this.categoryService.getCategories().subscribe((categories) => {
      this.categories.set(categories);
    });
  }

  ngOnInit() {
    this.loadCategories();
  }

  createForm = new FormGroup({
    name: new FormControl<string>('', [Validators.required, Validators.pattern(/\S/)]),
    categoryType: new FormControl<'INCOME' | 'EXPENSE' | null>(null, Validators.required),
  });

  createCategory() {
    this.createError.set(null);
    if (this.createForm.invalid) {
      return;
    }

    const { name, categoryType } = this.createForm.value;

    if (!name?.trim() || !categoryType) {
      return;
    }

    this.categoryService.createCategory({ name: name.trim(), categoryType }).subscribe({
      next: () => {
        this.loadCategories();
        this.showCreateForm.set(false);
        this.createForm.reset();
        this.createError.set(null);
      },
      error: (error) => {
        console.error('Failed to create category:', error);
        this.createError.set('Failed to create category. Please try again.');
      },
    });
  }

  editForm = new FormGroup({
    name: new FormControl<string>('', [Validators.required, Validators.pattern(/\S/)]),
    categoryType: new FormControl<'INCOME' | 'EXPENSE' | null>(null, Validators.required),
  });

  startEditing(category: CategoryResponse) {
    this.showCreateForm.set(false);
    this.editingCategoryId.set(category.id);
    this.editForm.setValue({
      name: category.name,
      categoryType: category.categoryType,
    });
  }

  cancelEditing() {
    this.editingCategoryId.set(null);
    this.editForm.reset();
  }
  saveEditing() {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    const categoryId = this.editingCategoryId();

    if (categoryId === null) {
      return;
    }

    const { name, categoryType } = this.editForm.value;

    if (!name?.trim() || !categoryType) {
      return;
    }

    this.editError.set(null);
    this.categoryService.updateCategory(categoryId, { name: name.trim(), categoryType }).subscribe({
      next: () => {
        this.cancelEditing();
        this.loadCategories();
      },
      error: (error) => {
        console.error('Failed to update category:', error);
        this.editError.set('Failed to update category. Please try again.');
      },
    });
  }

  onDelete(categoryId: number) {
    if (confirm('Are you sure you want to delete this category?')) {
      this.deleteError.set(null);
      this.categoryService.deleteCategory(categoryId).subscribe({
        next: () => {
          this.loadCategories();
        },
        error: (error) => {
          console.error('Failed to delete category:', error);
          this.deleteError.set('Failed to delete category. Please try again.');
        },
      });
    }
  }
}
