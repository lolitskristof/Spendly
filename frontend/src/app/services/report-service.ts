import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { DateRangeSummaryResponse } from '../models/date-range-summary-response';

@Service()
export class ReportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reports`;

  getMonthlyReport(year: number, month: number) {
    return this.http.get<DateRangeSummaryResponse>(
      `${this.apiUrl}/monthly?year=${year}&month=${month}`,
    );
  }

  getDateRangeReport(from: string, to: string) {
    return this.http.get<DateRangeSummaryResponse>(
      `${this.apiUrl}/date-range?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
    );
  }

  exportCsv(from: string, to: string) {
    return this.http.get(
      `${this.apiUrl}/export/csv?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`,
      {
        responseType: 'blob',
      },
    );
  }
}
