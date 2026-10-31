import { Service, inject } from '@angular/core';
import { CategoryResponse } from '../models/category-response';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { CreateCategoryRequest } from '../models/create-category-request';
import { UpdateCategoryRequest } from '../models/update-category-request';
@Service()
export class CategoryService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/categories`;

  getCategories() {
    return this.http.get<CategoryResponse[]>(this.apiUrl);
  }

  createCategory(request: CreateCategoryRequest) {
    return this.http.post<CategoryResponse>(this.apiUrl, request);
  }

  updateCategory(categoryId: number, request: UpdateCategoryRequest) {
    return this.http.patch<CategoryResponse>(`${this.apiUrl}/${categoryId}`, request);
  }

  deleteCategory(categoryId: number) {
    return this.http.delete<void>(`${this.apiUrl}/${categoryId}`);
  }
}
