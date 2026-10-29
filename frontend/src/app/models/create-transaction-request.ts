export interface CreateTransactionRequest {
  categoryId: number;
  amount: number;
  description?: string;
  transactionDate: string;
}
