export interface CreateSubscriptionRequest {
  categoryId: number;
  name: string;
  amount: number;
  billingPeriod: 'MONTHLY' | 'YEARLY';
  nextBillingDate: string;
}
