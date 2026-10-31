import { Component, inject, signal } from '@angular/core';
import { ReportService } from '../../services/report-service';
import { DateRangeSummaryResponse } from '../../models/date-range-summary-response';
import { CurrencyPipe } from '@angular/common';

@Component({
  imports: [CurrencyPipe],
  selector: 'app-reports',
  styleUrl: './reports.css',
  templateUrl: './reports.html',
})
export class Reports {
  private reportService = inject(ReportService);

  monthlyReport = signal<DateRangeSummaryResponse | null>(null);
  selectedYear = signal<number>(new Date().getFullYear());
  selectedMonth = signal<number>(new Date().getMonth() + 1);
  dateRangeReport = signal<DateRangeSummaryResponse | null>(null);
  fromDate = signal<string>('');
  toDate = signal<string>('');

  loadMonthlyReport() {
    const year = this.selectedYear();
    const month = this.selectedMonth();
    this.reportService.getMonthlyReport(year, month).subscribe((report) => {
      this.monthlyReport.set(report);
    });
  }

  ngOnInit() {
    this.loadMonthlyReport();
  }

  onMonthChange(value: string) {
    const month = Number(value);

    if (Number.isInteger(month) && month >= 1 && month <= 12) {
      this.selectedMonth.set(month);
      this.loadMonthlyReport();
    }
  }

  onYearChange(value: string) {
    const year = Number(value);

    if (Number.isInteger(year) && year >= 2000) {
      this.selectedYear.set(year);
      this.loadMonthlyReport();
    }
  }

  loadDateRangeReport() {
    const from = this.fromDate();
    const to = this.toDate();

    const fromDateTime = new Date(`${from}T00:00:00`).toISOString();
    const toDateTime = new Date(`${to}T23:59:59.999`).toISOString();

    if (from && to) {
      this.reportService.getDateRangeReport(fromDateTime, toDateTime).subscribe((report) => {
        this.dateRangeReport.set(report);
      });
    }
  }

  downloadCsv() {
    const from = this.fromDate();
    const to = this.toDate();

    if (!from || !to || from > to) {
      return;
    }

    const fromDateTime = new Date(`${from}T00:00:00`).toISOString();
    const toDateTime = new Date(`${to}T23:59:59.999`).toISOString();

    if (from && to) {
      this.reportService.exportCsv(fromDateTime, toDateTime).subscribe((blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `report_${from}_${to}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      });
    }
  }
}
