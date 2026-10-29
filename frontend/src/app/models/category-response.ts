export interface CategoryResponse {
  id: number;
  name: string;
  categoryType: 'INCOME' | 'EXPENSE';
  userId: number | null;
}
