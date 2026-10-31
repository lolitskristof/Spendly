import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard/dashboard';
import { Login } from './pages/login/login';
import { authGuard } from './guards/auth-guard';
import { Register } from './pages/register/register';
import { Transactions } from './pages/transactions/transactions';
import { Budgets } from './pages/budgets/budgets';
import { Subscriptions } from './pages/subscriptions/subscriptions';
import { Categories } from './pages/categories/categories';
import { Reports } from './pages/reports/reports';
import { Profile } from './pages/profile/profile';

export const routes: Routes = [
  { path: '', component: Dashboard, canActivate: [authGuard] },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'transactions', component: Transactions, canActivate: [authGuard] },
  { path: 'budgets', component: Budgets, canActivate: [authGuard] },
  { path: 'subscriptions', component: Subscriptions, canActivate: [authGuard] },
  { path: 'categories', component: Categories, canActivate: [authGuard] },
  { path: 'reports', component: Reports, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
];
