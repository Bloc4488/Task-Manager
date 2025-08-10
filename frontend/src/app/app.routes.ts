import { Routes } from '@angular/router';
import { AuthGuard } from './core/auth-guard';
import { Home } from './home/home';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { Tasks } from './tasks/tasks';
import { AccessDenied } from './auth/access-denied/access-denied';
import { Categories } from './categories/categories';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'access-denied', component: AccessDenied },
  { path: 'tasks', component: Tasks, canActivate: [AuthGuard] },
  { path: 'categories', component: Categories, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '' }
];
