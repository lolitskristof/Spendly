import { Routes } from '@angular/router';
import { Dashboard } from './pages/dashboard/dashboard';
import { Login } from './pages/login/login';
import { authGuard } from './guards/auth-guard';
import { Register } from './pages/register/register';
import { Transactions } from './pages/transactions/transactions';

export const routes: Routes = [
  { path: '', component: Dashboard, canActivate: [authGuard] },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'transactions', component: Transactions, canActivate: [authGuard] },
];
