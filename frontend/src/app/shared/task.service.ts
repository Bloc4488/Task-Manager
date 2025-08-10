import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Task, TaskRequest } from './models/task.model';
import { Page } from './models/page.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private apiUrl = 'http://localhost:8080/api/tasks';

  constructor(private http: HttpClient) {}

  create(task: TaskRequest): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, task).pipe(
        catchError(this.handleError)
    );
  }

  getAll(status?: string): Observable<Task[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    return this.http.get<Task[]>(this.apiUrl, { params }).pipe(
        catchError(this.handleError)
    );
  }

  getById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`).pipe(
        catchError(this.handleError)
    );
  }

  update(id: number, task: TaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, task).pipe(
        catchError(this.handleError)
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
        catchError(this.handleError)
    );
  }

  filter(status?: string, categoryId?: number, createdBefore?: string): Observable<Task[]> {
    let params = new HttpParams();
    if (status) params = params.set('status', status);
    if (categoryId) params = params.set('categoryId', categoryId.toString());
    if (createdBefore) params = params.set('createdBefore', createdBefore);
    return this.http.get<Task[]>(`${this.apiUrl}/filter`, { params }).pipe(
        catchError(this.handleError)
    );
  }

  getPaged(page: number = 0, size: number = 10, sort: string = 'id,asc'): Observable<Page<Task>> {
    let params = new HttpParams()
        .set('page', page.toString())
        .set('size', size.toString())
        .set('sort', sort);
    return this.http.get<Page<Task>>(`${this.apiUrl}/paged`, { params }).pipe(
        catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';
    if (error.error instanceof Object && error.error.message) {
      errorMessage = error.error.message;
    } else if (error.status === 400) {
      errorMessage = 'Invalid input. Please check the task details.';
    } else if (error.status === 401) {
      errorMessage = 'Unauthorized. Please log in again.';
    } else if (error.status === 403) {
      errorMessage = 'Access denied. You cannot modify this task or category.';
    } else if (error.status === 404) {
      errorMessage = 'Task or category not found.';
    } else if (error.status === 0) {
      errorMessage = 'Network error. Please check your connection.';
    }
    return throwError(() => new Error(errorMessage));
  }
}
