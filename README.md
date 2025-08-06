# Task Manager (Spring Boot Project)

This is a backend service for a task management application, built using Spring Boot.

## Tech Stack

- Java 17
- Spring Boot (Web, Security, JPA)
- PostgreSQL
- Docker
- JWT (implemented)
- Swagger (planned)

## Features

- User authentication (login/register) with JWT
- Task CRUD operations (create, read, update, delete)
- Role-based access (USER, ADMIN)
- Task filtering by status, category, and creation date
- Pagination and sorting for task lists
- Global exception handling

## How to Run

### 1. Prerequisites

- Docker installed
- Java 17 SDK

### 2. Start PostgreSQL

```bash
docker-compose up -d
```

### 3. Build and Run Application

- Clone the repository
- Run the application using Maven
```bash
./mvnw spring-boot:run
```

### ✅ Part 1: Initial

- Initial of project
- Docker Compose
- Test Controller: `ping`

### ✅ Part 2: JWT Authentication

- Implemented JWT-based login/register system
- AuthController: `auth/login`, `auth/register`
- JwtService handles token generation and validation
- JwtFilter protects private endpoints

### ✅ Part 3: Task CRUD

- Created Task entity with User relation
- Implemented TaskRequest/TaskResponse DTOs
- Added TaskService with create, getAll, update, delete
- Added TaskController: `tasks/`

### ✅ Part 4: Roles, Authorization and Filtering

- Added USER and ADMIN roles
- Restricted admin endpoints via role-based access
- Implemented filtering tasks by status with GET `/api/tasks?status=TODO`
- Added basic global exception handler

### ✅ Part 5: Categories, Pagination and Advanced Filtering

- Added Categories with CRUD operations
- Task filtering and pagination
- Secure task access by authenticated user