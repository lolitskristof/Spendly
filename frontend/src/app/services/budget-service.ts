import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { BudgetResponse } from '../models/budget-response';
import { CreateBudgetRequest } from '../models/create-budget-request';
import { UpdateBudgetRequest } from '../models/update-budget-request';

@Service()
export class BudgetService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/budget`;

  getBudgets(year: number, month: number) {
    return this.http.get<BudgetResponse[]>(`${this.apiUrl}?year=${year}&month=${month}`);
  }

  createBudget(request: CreateBudgetRequest) {
    return this.http.post<BudgetResponse>(this.apiUrl, request);
  }

  updateBudget(budgetId: number, request: UpdateBudgetRequest) {
    return this.http.patch<BudgetResponse>(`${this.apiUrl}/${budgetId}`, request);
  }

  deleteBudget(budgetId: number) {
    return this.http.delete<void>(`${this.apiUrl}/${budgetId}`);
  }
}
