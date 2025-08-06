# Task Manager (Spring Boot Project)

This is a backend service for a task management application, built using Spring Boot.

## Tech Stack

- Java 17
- Spring Boot (Web, Security, JPA)
- PostgreSQL
- Docker
- JWT (planned)
- Swagger (planned)

## How to Run

### 1. Start PostgreSQL

```bash
docker-compose up -d
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