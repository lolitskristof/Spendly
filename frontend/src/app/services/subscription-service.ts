import { Service, inject } from '@angular/core';
import { SubscriptionResponse } from '../models/subscription-response';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CreateSubscriptionRequest } from '../models/create-subscription-request';
import { UpdateSubscriptionRequest } from '../models/update-subscription-request';

@Service()
export class SubscriptionService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/subscription`;

  getSubscriptions() {
    return this.http.get<SubscriptionResponse[]>(this.apiUrl);
  }

  createSubscription(request: CreateSubscriptionRequest) {
    return this.http.post<SubscriptionResponse>(this.apiUrl, request);
  }

  updateSubscription(id: number, request: UpdateSubscriptionRequest) {
    return this.http.patch<SubscriptionResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteSubscription(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
