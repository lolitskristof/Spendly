export interface SubscriptionResponse {
  id: number;
  userId: number;
  categoryId: number;
  name: string;
  amount: number;
  period: string;
  nextBillingDate: string;
  status: string;
}
