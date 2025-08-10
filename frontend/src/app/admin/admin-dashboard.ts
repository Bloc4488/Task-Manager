import { Component, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { AdminService } from '../shared/admin.service';
import { User } from '../shared/models/user.model';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatCardModule,
    MatSnackBarModule
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard implements OnInit {
  users: User[] = [];
  displayedColumns: string[] = ['id', 'firstName', 'lastName', 'email', 'role'];

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.adminService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
      },
      error: (err) => {
        this.snackBar.open(err.message, 'Close', {
          duration: 5000,
          verticalPosition: 'top'
        });
      }
    });
  }
}
