import { Component, OnInit } from '@angular/core';
import {ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormControl} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { CategoryService } from '../shared/category.service';
import { Category } from '../shared/models/category.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatSnackBarModule,
    MatDialogModule
  ],
  templateUrl: './categories.html',
  styleUrl: './categories.scss'
})
export class Categories implements OnInit {
  categoryForm!: FormGroup;
  categories: Category[] = [];
  displayedColumns: string[] = ['name', 'description', 'actions'];
  isEditing = false;
  editingCategoryId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(3)]]
    });
    this.loadCategories();
  }

  loadCategories() {
    this.categoryService.getAll().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (err) => {
        this.snackBar.open(err.message, 'Close', {
          duration: 5000,
          verticalPosition: 'top'
        });
      }
    });
  }

  onSubmit() {
    if (this.categoryForm.invalid) {
      this.snackBar.open('Please fill in all fields correctly.', 'Close', {
        duration: 5000,
        verticalPosition: 'top'
      });
      return;
    }

    const category = this.categoryForm.value;
    if (this.isEditing && this.editingCategoryId) {
      this.categoryService.update(this.editingCategoryId, category).subscribe({
        next: () => {
          this.snackBar.open('Category updated successfully', 'Close', { duration: 3000 });
          this.loadCategories();
          this.resetForm();
        },
        error: (err) => {
          this.snackBar.open(err.message, 'Close', {
            duration: 5000,
            verticalPosition: 'top'
          });
        }
      });
    } else {
      this.categoryService.create(category).subscribe({
        next: () => {
          this.snackBar.open('Category created successfully', 'Close', { duration: 3000 });
          this.loadCategories();
          this.resetForm();
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

  editCategory(category: Category) {
    this.isEditing = true;
    this.editingCategoryId = category.id;
    this.categoryForm.patchValue({
      name: category.name,
      description: category.description
    });
  }

  deleteCategory(id: number) {
    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.delete(id).subscribe({
        next: () => {
          this.snackBar.open('Category deleted successfully', 'Close', { duration: 3000 });
          this.loadCategories();
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

  resetForm() {
    this.isEditing = false;
    this.editingCategoryId = null;
    this.categoryForm.reset();
  }

  get name(): FormControl{
    return this.categoryForm.get('name') as FormControl;
  }

  get description(): FormControl {
    return this.categoryForm.get('description') as FormControl;
  }
}
