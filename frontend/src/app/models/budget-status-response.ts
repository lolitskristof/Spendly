export interface BudgetStatusResponse {
  budgetId: number;
  categoryId: number;
  categoryName: string;
  budgetAmount: number;
  spentAmount: number;
  remainingAmount: number;
  exceeded: boolean;
}
