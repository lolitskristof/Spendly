import { Component } from '@angular/core';
import { TransactionService } from '../../services/transaction-service';
import { inject, signal } from '@angular/core';
import { TransactionResponse } from '../../models/transaction-response';
import { DatePipe, CurrencyPipe } from '@angular/common';
import { CategoryService } from '../../services/category-service';
import { CategoryResponse } from '../../models/category-response';

@Component({
  imports: [DatePipe, CurrencyPipe],
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
}
