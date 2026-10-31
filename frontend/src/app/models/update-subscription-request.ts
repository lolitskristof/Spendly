export interface UpdateSubscriptionRequest {
  categoryId?: number;
  name?: string;
  amount?: number;
  billingPeriod?: 'MONTHLY' | 'YEARLY';
  nextBillingDate?: string;
  status?: 'ACTIVE' | 'CANCELLED';
}
