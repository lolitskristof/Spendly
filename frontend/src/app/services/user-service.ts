import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { UserResponse } from '../models/user-response';

@Service()
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/users`;

  getMe() {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }
}
