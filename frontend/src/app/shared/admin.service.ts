import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { User } from './models/user.model';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';
    if (error.error instanceof Object && error.error.message) {
      errorMessage = error.error.message;
    } else if (error.status === 401) {
      errorMessage = 'Unauthorized. Please log in again.';
    } else if (error.status === 403) {
      errorMessage = 'Access denied. Admin privileges required.';
    } else if (error.status === 0) {
      errorMessage = 'Network error. Please check your connection.';
    }
    return throwError(() => new Error(errorMessage));
  }
}
