import { Component } from '@angular/core';
import { TransactionService } from '../../services/transaction-service';
import { inject, signal } from '@angular/core';
import { TransactionResponse } from '../../models/transaction-response';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { CategoryService } from '../../services/category-service';
import { CategoryResponse } from '../../models/category-response';
import { ReactiveFormsModule, Validators, FormGroup, FormControl } from '@angular/forms';

@Component({
  imports: [DatePipe, CurrencyPipe, ReactiveFormsModule],
  selector: 'app-transactions',
  styleUrl: './transactions.css',
  templateUrl: './transactions.html',
})
export class Transactions {
  private transactionService = inject(TransactionService);
  private categoryService = inject(CategoryService);

  transactions = signal<TransactionResponse[]>([]);
  categories = signal<CategoryResponse[]>([]);
  searchTerm = signal<string>('');
  selectedCategoryId = signal<number | null>(null);
  currentPage = 0;
  totalPages = signal<number>(0);
  showCreateForm = signal<boolean>(false);
  editingTransactionId = signal<number | null>(null);

  ngOnInit() {
    this.loadTransactions();
    this.loadCategories();
  }

  onSearch() {
    this.currentPage = 0;
    this.loadTransactions();
  }

  loadTransactions() {
    this.transactionService
      .getTransactions(this.currentPage, 10, this.searchTerm(), this.selectedCategoryId())
      .subscribe((response) => {
        this.transactions.set(response.content);
        this.totalPages.set(response.totalPages);
      });
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage = page;
      this.loadTransactions();
    }
  }

  loadCategories() {
    this.categoryService.getCategories().subscribe((categories) => {
      this.categories.set(categories);
    });
  }

  getCategoryName(categoryId: number): string {
    const category = this.categories().find((cat) => cat.id === categoryId);
    return category ? category.name : 'Unknown category';
  }

  onCategoryChange(value: string) {
    this.selectedCategoryId.set(value ? parseInt(value, 10) : null);
    this.currentPage = 0;
    this.loadTransactions();
  }

  createForm = new FormGroup({
    categoryId: new FormControl<number | null>(null, Validators.required),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    description: new FormControl<string>('', Validators.maxLength(255)),
    transactionDate: new FormControl<string>('', Validators.required),
  });

  editForm = new FormGroup({
    categoryId: new FormControl<number | null>(null, Validators.required),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    description: new FormControl<string>('', Validators.maxLength(255)),
    transactionDate: new FormControl<string>('', Validators.required),
  });

  createTransaction() {
    if (this.createForm.valid) {
      const { categoryId, amount, description, transactionDate } = this.createForm.value;
      if (categoryId && amount && transactionDate) {
        this.transactionService
          .createTransaction(
            categoryId,
            amount,
            description ?? '',
            new Date(transactionDate).toISOString(),
          )
          .subscribe(() => {
            this.createForm.reset();
            this.showCreateForm.set(false);
            this.currentPage = 0;
            this.loadTransactions();
          });
      }
    }
  }

  startEditing(transaction: TransactionResponse) {
    this.showCreateForm.set(false);
    this.editingTransactionId.set(transaction.id);
    const date = new Date(transaction.transactionDate);

    const localDateTime = new Date(date.getTime() - date.getTimezoneOffset() * 60_000)
      .toISOString()
      .slice(0, 16);

    this.editForm.setValue({
      categoryId: transaction.categoryId,
      amount: transaction.amount,
      description: transaction.description,
      transactionDate: localDateTime,
    });
  }

  cancelEditing() {
    this.editingTransactionId.set(null);
    this.editForm.reset();
  }

  saveEditing() {
    if (this.editForm.valid && this.editingTransactionId() !== null) {
      const { categoryId, amount, description, transactionDate } = this.editForm.value;
      if (categoryId && amount && transactionDate) {
        this.transactionService
          .updateTransaction(this.editingTransactionId()!, {
            categoryId,
            amount,
            description: description ?? '',
            transactionDate: new Date(transactionDate).toISOString(),
          })
          .subscribe(() => {
            this.cancelEditing();
            this.loadTransactions();
          });
      }
    }
  }

  onDelete(transactionId: number) {
    if (confirm('Are you sure you want to delete this transaction?')) {
      this.transactionService.deleteTransaction(transactionId).subscribe(() => {
        this.loadTransactions();
      });
    }
  }
}
