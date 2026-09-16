# Taskinator

A backend REST API for collaborative project and task management, built with Spring Boot.

## Features

- User registration and authentication with JWT access tokens and httpOnly refresh-token cookies.
- Project lifecycle management (create, update, search, delete).
- Task management within projects.
- Project membership with custom roles and granular permissions.
- Role-based access control via Spring Security.
- Database versioning with Flyway.
- Dockerized deployment support.

## Tech Stack

- Java 17
- Spring Boot 4.0.6 (Web MVC, Security, Data JPA, Validation)
- PostgreSQL (runtime database)
- H2 (test database)
- Flyway (database migrations)
- JJWT (token handling)
- Maven Wrapper
- Docker & Docker Compose
- GitHub Actions (CI)

## Prerequisites

- JDK 17
- Maven 3.9+ or use `./mvnw`
- Docker and Docker Compose (for containerized setup)
- A running PostgreSQL instance (if not using Docker)

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://localhost:5432/taskinator` |
| `DB_USERNAME` | PostgreSQL username | `taskinator` |
| `DB_PASSWORD` | PostgreSQL password | `taskinator` |
| `DB_NAME` | Database name (used by Docker Compose) | `taskinator` |
| `DB_PORT` | Host port for PostgreSQL (used by Docker Compose) | `5432` |
| `BACKEND_PORT` | Host port for the backend service (used by Docker Compose) | `8080` |
| `JWT_SECRET` | Secret key for signing JWTs | (strong random string) |
| `JWT_ACCESS_TOKEN_EXPIRATION_MS` | Access token TTL in milliseconds | `900000` |
| `JWT_REFRESH_TOKEN_EXPIRATION_MS` | Refresh token TTL in milliseconds | `604800000` |
| `SPRING_PROFILES_ACTIVE` | Spring profile (`dev` or `prod`) | `dev` |

Sample `.env` file:

```env
DB_URL=jdbc:postgresql://localhost:5432/taskinator
DB_USERNAME=taskinator
DB_PASSWORD=taskinator
DB_NAME=taskinator
DB_PORT=5432
BACKEND_PORT=8080
JWT_SECRET=change-me-in-production
JWT_ACCESS_TOKEN_EXPIRATION_MS=900000
JWT_REFRESH_TOKEN_EXPIRATION_MS=604800000
SPRING_PROFILES_ACTIVE=dev
DB_SCHEMA=public
DB_APP_ROLE=taskinator
DB_MAX_POOL_SIZE=10
DB_MIN_IDLE=2
FLYWAY_USERNAME=taskinator
FLYWAY_PASSWORD=taskinator
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200,http://localhost:8080
```

## Running Locally

1. Create `.env` at the project root.
2. Start PostgreSQL.
3. Run:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

4. The API will be available at `http://localhost:8080`.

## Running with Docker Compose

```bash
docker compose up --build
```

- The `prod` profile is set automatically inside the container.
- PostgreSQL data is persisted in the `taskinator_pgdata` volume.

## API Endpoints

### Authentication — `POST /api/v1/auth`

- `/register`
- `/login`
- `/refresh`
- `/logout`

### Current User — `/api/v1/me`

- `GET /`
- `POST /email`
- `POST /password`

### Projects — `/api/v1/projects`

- `GET /`
- `GET /search`
- `POST /`
- `PUT /{projectId}`
- `DELETE /{projectId}`

### Tasks — `/api/v1/projects/{projectId}/tasks`

- `GET /`
- `GET /{taskId}`
- `POST /`
- `PUT /{taskId}`
- `DELETE /{taskId}`

### Project Members — `/api/v1/projects/{projectId}/members`

- `GET /`
- `POST /`
- `PUT /{userId}`
- `DELETE /{userId}`

### Project Roles — `/api/v1/projects/{projectId}/roles`

- `GET /`
- `POST /`
- `PUT /{roleId}`
- `DELETE /{roleId}`

## Authentication

- The access token is returned in the login, register, and refresh response bodies and must be sent in the `Authorization: Bearer <token>` header for protected endpoints.
- The refresh token is returned as an httpOnly, Secure, SameSite=Strict cookie scoped to `/api/v1/auth`.
- The `dev` profile sets `application.cookie.secure=false` to simplify local testing.

## Testing

Run unit tests:

```bash
./mvnw test
```

Run unit and integration tests:

```bash
./mvnw verify
```

- Integration tests (`*IT.java`) use H2 and Database Rider/DBUnit.
- CI runs `./mvnw verify` on every push and pull request to `main`.

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration` and run automatically on startup when Flyway is enabled.

## Project Structure

- `domain` — entities, repositories, enums, and value objects.
- `application` — services, DTOs, and business logic.
- `infrastructure` — security, configuration, and cross-cutting concerns.
- `web` — controllers and request DTOs.
- `resources/db/migration` — Flyway SQL migrations.

## CI/CD

GitHub Actions workflow is defined in `.github/workflows/ci.yml`. It builds the project with JDK 17 Temurin and runs `./mvnw verify`.
