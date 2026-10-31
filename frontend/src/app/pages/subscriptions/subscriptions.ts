import { Component, inject, signal } from '@angular/core';
import { SubscriptionService } from '../../services/subscription-service';
import { CategoryService } from '../../services/category-service';
import { SubscriptionResponse } from '../../models/subscription-response';
import { CategoryResponse } from '../../models/category-response';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ReactiveFormsModule, Validators, FormGroup, FormControl } from '@angular/forms';

@Component({
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule],
  selector: 'app-subscriptions',
  styleUrl: './subscriptions.css',
  templateUrl: './subscriptions.html',
})
export class Subscriptions {
  private subscriptionService = inject(SubscriptionService);
  private categoryService = inject(CategoryService);

  subscriptions = signal<SubscriptionResponse[]>([]);
  categories = signal<CategoryResponse[]>([]);

  editingSubscriptionId = signal<number | null>(null);

  showCreateForm = signal(false);

  loadSubscriptions() {
    this.subscriptionService.getSubscriptions().subscribe((subscriptions) => {
      this.subscriptions.set(subscriptions);
    });
  }

  loadCategories() {
    this.categoryService.getCategories().subscribe((categories) => {
      this.categories.set(categories);
    });
  }

  getCategoryName(categoryId: number): string {
    const category = this.categories().find((cat) => cat.id === categoryId);
    return category ? category.name : 'Unknown category';
  }

  ngOnInit() {
    this.loadSubscriptions();
    this.loadCategories();
  }

  createForm = new FormGroup({
    categoryId: new FormControl<number | null>(null, Validators.required),

    name: new FormControl<string>('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),

    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),

    billingPeriod: new FormControl<'MONTHLY' | 'YEARLY'>('MONTHLY', Validators.required),

    nextBillingDate: new FormControl<string>('', Validators.required),
  });

  createSubscription() {
    if (this.createForm.valid) {
      const request = {
        categoryId: this.createForm.value.categoryId!,
        name: this.createForm.value.name!.trim(),
        amount: this.createForm.value.amount!,
        billingPeriod: this.createForm.value.billingPeriod!,
        nextBillingDate: this.createForm.value.nextBillingDate!,
      };

      this.subscriptionService.createSubscription(request).subscribe(() => {
        this.loadSubscriptions();
        this.showCreateForm.set(false);
        this.createForm.reset({
          categoryId: null,
          name: '',
          amount: null,
          billingPeriod: 'MONTHLY',
          nextBillingDate: '',
        });
      });
    }
  }

  editForm = new FormGroup({
    categoryId: new FormControl<number | null>(null, Validators.required),
    name: new FormControl<string>('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    billingPeriod: new FormControl<'MONTHLY' | 'YEARLY'>('MONTHLY', Validators.required),
    nextBillingDate: new FormControl<string>('', Validators.required),
    status: new FormControl<string>('', Validators.required),
  });

  startEditing(subscription: SubscriptionResponse) {
    this.showCreateForm.set(false);

    this.editForm.setValue({
      categoryId: subscription.categoryId,
      name: subscription.name,
      amount: subscription.amount,
      billingPeriod: subscription.period as 'MONTHLY' | 'YEARLY',
      nextBillingDate: subscription.nextBillingDate,
      status: subscription.status,
    });

    this.editingSubscriptionId.set(subscription.id);
  }

  cancelEditing() {
    this.editingSubscriptionId.set(null);
    this.editForm.reset();
  }

  saveEditing() {
    if (this.editForm.valid && this.editingSubscriptionId() !== null) {
      const request = {
        categoryId: this.editForm.value.categoryId!,
        name: this.editForm.value.name!.trim(),
        amount: this.editForm.value.amount!,
        billingPeriod: this.editForm.value.billingPeriod!,
        nextBillingDate: this.editForm.value.nextBillingDate!,
        status: this.editForm.value.status as 'ACTIVE' | 'CANCELLED',
      };

      this.subscriptionService
        .updateSubscription(this.editingSubscriptionId()!, request)
        .subscribe(() => {
          this.loadSubscriptions();
          this.cancelEditing();
        });
    }
  }

  onDelete(subscriptionId: number) {
    if (confirm('Are you sure you want to delete this subscription?')) {
      this.subscriptionService.deleteSubscription(subscriptionId).subscribe({
        next: () => {
          this.loadSubscriptions();
        },
        error: (error) => {
          console.error('Failed to delete subscription:', error);
        },
      });
    }
  }
}
