import { Injectable } from "@angular/core";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {finalize, Observable, tap, throwError} from "rxjs";
import { AuthRequest, AuthResponse, RegisterRequest } from './models/auth.model';
import { Router } from '@angular/router';
import {catchError} from "rxjs/operators";
import {User} from './models/user.model';
import {LoadingService} from './loading.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient, private router: Router, private loadingService: LoadingService) {}

  login(credentials: AuthRequest): Observable<AuthResponse> {
    this.loadingService.show();
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        this.saveToken(response.token);
        this.router.navigate(['/tasks']);
      }),
        catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  register(user: RegisterRequest): Observable<AuthResponse> {
    this.loadingService.show();
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, user).pipe(
      tap(response => {
        this.saveToken(response.token);
        this.router.navigate(['/tasks']);
      }),
        catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  getCurrentUser(): Observable<User> {
    this.loadingService.show();
    return this.http.get<User>(`http://localhost:8080/api/users/me`).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  updateUser(user: { firstName: string, lastName: string, email: string}): Observable<User> {
    this.loadingService.show();
    return this.http.put<User>(`http://localhost:8080/api/users/me`, user).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  changePassword(passwords: { currentPassword: string, newPassword: string, confirmPassword: string }): Observable<void> {
    this.loadingService.show();
    return this.http.put<void>(`http://localhost:8080/api/users/me/password`, passwords).pipe(
      catchError(this.handleError),
      finalize(() => this.loadingService.hide())
    );
  }

  saveToken(token: string): void {
    localStorage.setItem('token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    const token = this.getToken();
    if (token) {
      try {
        const decoded = JSON.parse(atob(token.split('.')[1]));
        return decoded.role === 'ADMIN';
      } catch (e) {
        return false;
      }
    }
    return false;
  }

  logout(): void {
    this.loadingService.show();
    localStorage.removeItem('token');
    this.router.navigate(['/']);
    this.loadingService.hide();
  }

  getUserRole(): string {
    const token = this.getToken();
    if (token) {
      try {
        const decoded = JSON.parse(atob(token.split('.')[1]));
        return decoded.role;
      }
      catch (e) {
        return '';
      }
    }
    return '';
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An unexpected error occurred';
    if (error.error instanceof Object && error.error.message) {
      errorMessage = error.error.message;
    } else if (error.status === 400) {
      errorMessage = 'Invalid input. Please check your email and password.';
    } else if (error.status === 401) {
      errorMessage = 'Invalid email or password.';
    } else if (error.status === 404) {
      errorMessage = 'User not found.';
    } else if (error.status === 409) {
      errorMessage = 'Email already exists.';
    } else if (error.status === 0) {
      errorMessage = 'Network error. Please check your connection.';
    }
    return throwError(() => new Error(errorMessage));
  }
}
