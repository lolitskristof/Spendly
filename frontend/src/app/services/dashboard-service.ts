import { MonthlySummaryResponse } from '../models/monthly-summary-response';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Service, inject } from '@angular/core';
import { CategoryExpenseSummary } from '../models/category-expense-summary';

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
}
