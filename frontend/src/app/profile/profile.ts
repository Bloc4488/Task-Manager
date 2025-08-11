import { Component, OnInit } from '@angular/core';
import {ReactiveFormsModule, FormBuilder, FormGroup, Validators, Form, FormControl} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { AuthService } from '../shared/auth.service';
import { User } from '../shared/models/user.model';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
    RouterModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  user: User | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });

    this.loadUser();
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { mismatch: true };
  }

  loadUser() {
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.profileForm.patchValue({
          firstName: user.firstName,
          lastName: user.lastName,
          email: user.email
        });
      },
      error: (err) => {
        this.snackBar.open(err.message, 'Close', {
          duration: 5000,
          verticalPosition: 'top'
        });
      }
    });
  }

  onUpdateProfile() {
    if (this.profileForm.invalid) {
      this.snackBar.open('Please fill in all fields correctly.', 'Close', {
        duration: 5000,
        verticalPosition: 'top'
      });
      return;
    }

    this.authService.updateUser(this.profileForm.value).subscribe({
      next: (user) => {
        this.user = user;
        this.snackBar.open('Profile updated successfully', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.snackBar.open(err.message, 'Close', {
          duration: 5000,
          verticalPosition: 'top'
        });
      }
    });
  }

  onChangePassword() {
    if (this.passwordForm.invalid || this.passwordForm.hasError('mismatch')) {
      this.snackBar.open('Please fill in all fields correctly and ensure passwords match.', 'Close', {
        duration: 5000,
        verticalPosition: 'top'
      });
      return;
    }

    this.authService.changePassword(this.passwordForm.value).subscribe({
      next: () => {
        this.snackBar.open('Password changed successfully', 'Close', { duration: 3000 });
        this.passwordForm.reset();
      },
      error: (err) => {
        this.snackBar.open(err.message, 'Close', {
          duration: 5000,
          verticalPosition: 'top'
        });
      }
    });
  }

  get firstName(): FormControl {
    return this.profileForm.get('firstName') as FormControl;
  }

  get lastName(): FormControl {
    return this.profileForm.get('lastName') as FormControl;
  }

  get email(): FormControl {
    return this.profileForm.get('email') as FormControl;
  }

  get currentPassword(): FormControl {
    return this.passwordForm.get('currentPassword') as FormControl;
  }

  get newPassword(): FormControl {
    return this.passwordForm.get('newPassword') as FormControl;
  }

  get confirmPassword(): FormControl {
    return this.passwordForm.get('confirmPassword') as FormControl;
  }
}
