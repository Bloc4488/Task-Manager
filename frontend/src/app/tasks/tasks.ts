import { Component, OnInit } from '@angular/core';
import {ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormControl} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { CommonModule } from '@angular/common';
import { TaskService } from '../shared/task.service';
import { CategoryService } from '../shared/category.service';
import { Task, Status } from '../shared/models/task.model';
import { Category } from '../shared/models/category.model';
import { Page } from '../shared/models/page.model';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTableModule,
    MatSnackBarModule,
    MatPaginatorModule
  ],
  templateUrl: './tasks.html',
  styleUrl: './tasks.scss'
})
export class Tasks implements OnInit {
  taskForm!: FormGroup;
  filterForm!: FormGroup;
  tasks: Task[] = [];
  categories: Category[] = [];
  displayedColumns: string[] = ['title', 'description', 'status', 'categoryName', 'createdAt', 'actions'];
  statusOptions = Object.values(Status);
  page: Page<Task> = { content: [], totalElements: 0, totalPages: 0, size: 10, number: 0, numberOfElements: 0, first: true, last: false, empty: true };
  isEditing = false;
  editingTaskId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private categoryService: CategoryService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.required, Validators.minLength(3)]],
      status: [Status.TODO, [Validators.required]],
      categoryId: [null, [Validators.required]]
    });

    this.filterForm = this.fb.group({
      status: [''],
      categoryId: [''],
      createdBefore: ['']
    });

    this.loadCategories();
    this.loadTasks();
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

  loadTasks(page: number = 0, size: number = 10, sort: string = 'id,asc') {
    this.taskService.getPaged(page, size, sort).subscribe({
      next: (page) => {
        this.page = page;
        this.tasks = page.content;
      },
      error: (err) => {
        this.snackBar.open(err.message, 'Close', {
          duration: 5000,
          verticalPosition: 'top'
        });
      }
    });
  }

  onPageChange(event: PageEvent) {
    this.loadTasks(event.pageIndex, event.pageSize);
  }

  onSubmit() {
    if (this.taskForm.invalid) {
      this.snackBar.open('Please fill in all fields correctly.', 'Close', {
        duration: 5000,
        verticalPosition: 'top'
      });
      return;
    }

    const task = this.taskForm.value;
    if (this.isEditing && this.editingTaskId) {
      this.taskService.update(this.editingTaskId, task).subscribe({
        next: () => {
          this.snackBar.open('Task updated successfully', 'Close', { duration: 3000 });
          this.loadTasks(this.page.number, this.page.size);
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
      this.taskService.create(task).subscribe({
        next: () => {
          this.snackBar.open('Task created successfully', 'Close', { duration: 3000 });
          this.loadTasks(this.page.number, this.page.size);
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

  applyFilter() {
    const { status, categoryId, createdBefore } = this.filterForm.value;
    this.taskService.filter(status || undefined, categoryId || undefined, createdBefore || undefined).subscribe({
      next: (tasks) => {
        this.tasks = tasks;
        this.page = { content: tasks, totalElements: tasks.length, totalPages: 1, size: tasks.length, number: 0, numberOfElements: tasks.length, first: true, last: true, empty: tasks.length === 0 };
      },
      error: (err) => {
        this.snackBar.open(err.message, 'Close', {
          duration: 5000,
          verticalPosition: 'top'
        });
      }
    });
  }

  editTask(task: Task) {
    this.isEditing = true;
    this.editingTaskId = task.id;
    this.taskForm.patchValue({
      title: task.title,
      description: task.description,
      status: task.status,
      categoryId: this.categories.find(c => c.name === task.categoryName)?.id
    });
  }

  deleteTask(id: number) {
    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.delete(id).subscribe({
        next: () => {
          this.snackBar.open('Task deleted successfully', 'Close', { duration: 3000 });
          this.loadTasks(this.page.number, this.page.size);
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
    this.editingTaskId = null;
    this.taskForm.reset({ status: Status.TODO });
  }

  resetFilter() {
    this.filterForm.reset();
    this.loadTasks();
  }

  get title(): FormControl {
    return this.taskForm.get('title') as FormControl;
  }

  get description(): FormControl {
    return this.taskForm.get('description') as FormControl;
  }

  get status(): FormControl {
    return this.taskForm.get('status') as FormControl;
  }

  get categoryId(): FormControl {
    return this.taskForm.get('categoryId') as FormControl;
  }
}
