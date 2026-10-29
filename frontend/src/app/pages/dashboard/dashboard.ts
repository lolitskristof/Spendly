import { Component, inject, signal } from '@angular/core';
import { DashboardService } from '../../services/dashboard-service';
import { MonthlySummaryResponse } from '../../models/monthly-summary-response';
import { CurrencyPipe } from '@angular/common';
import { CategoryExpenseSummary } from '../../models/category-expense-summary';
import { BudgetStatusResponse } from '../../models/budget-status-response';
import { SubscriptionResponse } from '../../models/subscription-response';

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
  budgetStatus = signal<BudgetStatusResponse[] | null>(null);
  upcomingSubscriptions = signal<SubscriptionResponse[] | null>(null);

  selectedYear = new Date().getFullYear();
  selectedMonth = new Date().getMonth() + 1;

  ngOnInit() {
    this.loadDashboardData();
    this.getUpcomingSubscriptions();
  }

  loadDashboardData() {
    this.loadMonthlySummary();
    this.getMonthlyBudgetStatus();
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

  getMonthlyBudgetStatus() {
    this.dashboardService
      .getMonthlyBudgetStatus(this.selectedYear, this.selectedMonth)
      .subscribe((status) => {
        this.budgetStatus.set(status);
      });
  }

  getUpcomingSubscriptions() {
    this.dashboardService.getUpcomingSubscriptions().subscribe((subscriptions) => {
      this.upcomingSubscriptions.set(subscriptions);
    });
  }
}
