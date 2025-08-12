import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import {finalize, Observable, throwError} from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Category, CategoryRequest } from './models/category.model';
import {LoadingService} from './loading.service';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private apiUrl = 'http://localhost:8080/api/category';

  constructor(private http: HttpClient, private loadingService: LoadingService) { }

  create(category: CategoryRequest): Observable<Category> {
    this.loadingService.show();
    return this.http.post<Category>(this.apiUrl, category).pipe(
        catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  getAll(): Observable<Category[]> {
    this.loadingService.show();
    return this.http.get<Category[]>(this.apiUrl).pipe(
        catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  getById(id: number): Observable<Category> {
    this.loadingService.show();
    return this.http.get<Category>(`${this.apiUrl}/${id}`).pipe(
        catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  update(id: number, category: CategoryRequest): Observable<Category> {
    this.loadingService.show();
    return this.http.put<Category>(`${this.apiUrl}/${id}`, category).pipe(
        catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  delete(id: number): Observable<void> {
    this.loadingService.show();
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
        catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';
    if (error.error instanceof Object && error.error.message) {
      errorMessage = error.error.message;
    } else if (error.status === 400) {
      errorMessage = 'Invalid input. Please check the category details.';
    } else if (error.status === 401) {
      errorMessage = 'Unauthorized. Please log in again.';
    } else if (error.status === 403) {
      errorMessage = 'Access denied.';
    } else if (error.status === 404) {
      errorMessage = 'Category not found.';
    } else if (error.status === 409) {
      errorMessage = 'Category name already exists.';
    } else if (error.status === 0) {
      errorMessage = 'Network error. Please check your connection.';
    }
    return throwError(() => new Error(errorMessage));
  }
}
