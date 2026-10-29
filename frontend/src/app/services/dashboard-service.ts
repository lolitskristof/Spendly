import { MonthlySummaryResponse } from '../models/monthly-summary-response';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Service, inject } from '@angular/core';
import { CategoryExpenseSummary } from '../models/category-expense-summary';
import { BudgetStatusResponse } from '../models/budget-status-response';
import { SubscriptionResponse } from '../models/subscription-response';

@Service()
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/dashboard`;

  getMonthlySummary(year: number, month: number) {
    return this.http.get<MonthlySummaryResponse>(
      `${this.apiUrl}/monthly-summary?year=${year}&month=${month}`,
    );
  }

  getCategoryExpenseSummary(year: number, month: number) {
    return this.http.get<CategoryExpenseSummary[]>(
      `${this.apiUrl}/category-expenses?year=${year}&month=${month}`,
    );
  }

  getMonthlyBudgetStatus(year: number, month: number) {
    return this.http.get<BudgetStatusResponse[]>(
      `${this.apiUrl}/budget-status?year=${year}&month=${month}`,
    );
  }

  getUpcomingSubscriptions() {
    return this.http.get<SubscriptionResponse[]>(`${this.apiUrl}/upcoming-subscriptions`);
  }
}
