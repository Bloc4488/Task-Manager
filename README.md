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