import { Component, signal, inject } from '@angular/core';
import { BudgetService } from '../../services/budget-service';
import { CategoryService } from '../../services/category-service';
import { BudgetResponse } from '../../models/budget-response';
import { CategoryResponse } from '../../models/category-response';
import { CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, Validators, FormGroup, FormControl } from '@angular/forms';

@Component({
  imports: [CurrencyPipe, ReactiveFormsModule],
  selector: 'app-budgets',
  styleUrl: './budgets.css',
  templateUrl: './budgets.html',
})
export class Budgets {
  private budgetService = inject(BudgetService);
  private categoryService = inject(CategoryService);

  monthNames = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
  ];

  budgets = signal<BudgetResponse[]>([]);
  categories = signal<CategoryResponse[]>([]);
  selectedYear = signal<number>(new Date().getFullYear());
  selectedMonth = signal<number>(new Date().getMonth() + 1);
  editingBudgetId = signal<number | null>(null);

  showCreateForm = signal<boolean>(false);

  ngOnInit() {
    this.loadBudgets();
    this.loadCategories();
  }

  loadBudgets() {
    const year = this.selectedYear();
    const month = this.selectedMonth();
    this.budgetService.getBudgets(year, month).subscribe((budgets) => {
      this.budgets.set(budgets);
    });
  }

  loadCategories() {
    this.categoryService.getCategories().subscribe((categories) => {
      this.categories.set(categories);
    });
  }

  getCategoryName(categoryId: number): string {
    const category = this.categories().find((cat) => cat.id === categoryId);
    return category ? category.name : 'Unknown Category';
  }

  onMonthChange(value: string) {
    const month = parseInt(value, 10);
    if (!isNaN(month) && month >= 1 && month <= 12) {
      this.selectedMonth.set(month);
      this.loadBudgets();
    }
  }

  onYearChange(value: string) {
    const year = parseInt(value, 10);
    if (!isNaN(year) && year >= 2000 && year <= 2100) {
      this.selectedYear.set(year);
      this.loadBudgets();
    }
  }

  createForm = new FormGroup({
    categoryId: new FormControl<number | null>(null, Validators.required),

    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    year: new FormControl(this.selectedYear(), [
      Validators.required,
      Validators.min(2000),
      Validators.max(2100),
    ]),
    month: new FormControl(this.selectedMonth(), [
      Validators.required,
      Validators.min(1),
      Validators.max(12),
    ]),
  });

  createBudget() {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const { categoryId, amount, year, month } = this.createForm.value;

    if (categoryId == null || amount == null || year == null || month == null) {
      return;
    }

    this.budgetService.createBudget({ categoryId, amount, year, month }).subscribe({
      next: () => {
        this.selectedYear.set(year);
        this.selectedMonth.set(month);

        this.showCreateForm.set(false);

        this.createForm.reset({
          categoryId: null,
          amount: null,
          year,
          month,
        });

        this.loadBudgets();
      },
      error: (error) => {
        console.error('Failed to create budget:', error);
      },
    });
  }

  editForm = new FormGroup({
    categoryId: new FormControl<number | null>(null, Validators.required),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    year: new FormControl(this.selectedYear(), [
      Validators.required,
      Validators.min(2000),
      Validators.max(2100),
    ]),
    month: new FormControl(this.selectedMonth(), [
      Validators.required,
      Validators.min(1),
      Validators.max(12),
    ]),
  });

  startEditing(budget: BudgetResponse) {
    this.showCreateForm.set(false);
    this.editingBudgetId.set(budget.id);
    this.editForm.setValue({
      categoryId: budget.categoryId,
      amount: budget.amount,
      year: budget.year,
      month: budget.month,
    });
  }

  cancelEditing() {
    this.editingBudgetId.set(null);
    this.editForm.reset();
  }

  saveEditing() {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    const budgetId = this.editingBudgetId();
    if (budgetId == null) {
      return;
    }

    const { categoryId, amount, year, month } = this.editForm.value;

    if (categoryId == null || amount == null || year == null || month == null) {
      return;
    }

    this.budgetService.updateBudget(budgetId, { categoryId, amount, year, month }).subscribe({
      next: () => {
        this.selectedYear.set(year);
        this.selectedMonth.set(month);

        this.cancelEditing();

        this.loadBudgets();
      },
      error: (error) => {
        console.error('Failed to update budget:', error);
      },
    });
  }

  onDelete(budgetId: number) {
    if (confirm('Are you sure you want to delete this budget?')) {
      this.budgetService.deleteBudget(budgetId).subscribe({
        next: () => {
          this.loadBudgets();
        },
        error: (error) => {
          console.error('Failed to delete budget:', error);
        },
      });
    }
  }
}
