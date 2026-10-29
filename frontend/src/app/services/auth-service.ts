import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { LoginResponse } from '../models/login-response';
import { Router } from '@angular/router';
import { Service } from '@angular/core';
import { RegisterResponse } from '../models/register-response';

@Service()
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private apiUrl = `${environment.apiUrl}/auth`;

  login(email: string, password: string) {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, {
      email: email,
      password: password,
    });
  }

  register(email: string, password: string, displayName: string) {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, {
      email: email,
      password: password,
      displayName: displayName,
    });
  }

  logout() {
    localStorage.removeItem('token');
    return this.router.navigate(['/login']);
  }
}
