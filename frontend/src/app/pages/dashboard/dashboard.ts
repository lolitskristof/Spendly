import { Component, inject, signal } from '@angular/core';
import { DashboardService } from '../../services/dashboard-service';
import { MonthlySummaryResponse } from '../../models/monthly-summary-response';
import { CurrencyPipe } from '@angular/common';
import { CategoryExpenseSummary } from '../../models/category-expense-summary';

@Component({
  imports: [CurrencyPipe],
  selector: 'app-dashboard',
  styleUrl: './dashboard.css',
  templateUrl: './dashboard.html',
})
export class Dashboard {
  private dashboardService = inject(DashboardService);

  monthlySummary = signal<MonthlySummaryResponse | null>(null);
  categoryExpenseSummary = signal<CategoryExpenseSummary[] | null>(null);

  selectedYear = new Date().getFullYear();
  selectedMonth = new Date().getMonth() + 1;

  ngOnInit() {
    this.loadDashboardData();
  }

  loadDashboardData() {
    this.loadMonthlySummary();
    this.getCategoryExpenseSummary();
  }

  loadMonthlySummary() {
    this.dashboardService
      .getMonthlySummary(this.selectedYear, this.selectedMonth)
      .subscribe((summary) => {
        this.monthlySummary.set(summary);
      });
  }

  getCategoryExpenseSummary() {
    this.dashboardService
      .getCategoryExpenseSummary(this.selectedYear, this.selectedMonth)
      .subscribe((summary) => {
        this.categoryExpenseSummary.set(summary);
      });
  }
}
