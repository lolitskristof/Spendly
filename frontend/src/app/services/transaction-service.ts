import { Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { inject } from '@angular/core';
import { PageResponse } from '../models/page-response';
import { TransactionResponse } from '../models/transaction-response';

@Service()
export class TransactionService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/transaction`;

  getTransactions(page: number, size: number, search?: string, categoryId?: number | null) {
    let url = `${this.apiUrl}?page=${page}&size=${size}&search=${encodeURIComponent(search ?? '')}`;

    if (categoryId != null) {
      url += `&categoryId=${categoryId}`;
    }

    return this.http.get<PageResponse<TransactionResponse>>(url);
  }
}
