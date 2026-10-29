import { Service, inject } from '@angular/core';
import { CategoryResponse } from '../models/category-response';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Service()
export class CategoryService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/categories`;

  getCategories() {
    return this.http.get<CategoryResponse[]>(this.apiUrl);
  }
}
