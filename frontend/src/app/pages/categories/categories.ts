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
    if (this.createForm.invalid) {
      return;
    }

    const { name, categoryType } = this.createForm.value;

    if (!name?.trim() || !categoryType) {
      return;
    }

    this.categoryService.createCategory({ name: name.trim(), categoryType }).subscribe(() => {
      this.loadCategories();
      this.showCreateForm.set(false);
      this.createForm.reset();
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

    this.categoryService.updateCategory(categoryId, { name: name.trim(), categoryType }).subscribe({
      next: () => {
        this.cancelEditing();
        this.loadCategories();
      },
      error: (error) => {
        console.error('Failed to update category:', error);
      },
    });
  }

  onDelete(categoryId: number) {
    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.deleteCategory(categoryId).subscribe({
        next: () => {
          this.loadCategories();
        },
        error: (error) => {
          console.error('Failed to delete category:', error);
        },
      });
    }
  }
}
