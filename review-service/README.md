# Review Service

Spring Boot microservice for managing course reviews in an LMS platform. Students submit and manage their own reviews; admins moderate content; public endpoints expose active reviews and rating summaries.

## Tech Stack

- Java 21
- Spring Boot 3.3.x
- Spring Security + JWT
- Spring Data JPA (Hibernate `ddl-auto: update`)
- PostgreSQL
- OpenFeign (enrollment & optional user service)
- Lombok, Validation, SpringDoc OpenAPI

## Prerequisites

- JDK 21+
- Gradle (wrapper included: `gradlew`)
- PostgreSQL 14+

## Database Setup

```sql
CREATE DATABASE review_db;
```

Hibernate creates/updates the `reviews` table automatically on startup via `spring.jpa.hibernate.ddl-auto=update`.

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `review_db` | Database name |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | (see `application.yml`) | HMAC secret (plain text or Base64) |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiry (informational) |
| `ENROLLMENT_SERVICE_URL` | `http://localhost:8082` | Enrollment service base URL |
| `USER_SERVICE_URL` | `http://localhost:8081` | User service base URL |
| `USER_SERVICE_ENABLED` | `false` | Set `true` to resolve student names via Feign |

## Run Locally

```bash
cd review-service
mvn spring-boot:run
```

Service listens on **port 8083**.

- Swagger UI: http://localhost:8083/swagger-ui.html
- OpenAPI JSON: http://localhost:8083/api-docs

## JWT Claims (Assumed)

Tokens are validated with the configured `JWT_SECRET`. Expected claims:

| Claim | Type | Usage |
|-------|------|--------|
| `userId` | number | Authenticated user ID (falls back to `sub`) |
| `roles` | string or array | e.g. `STUDENT`, `ADMIN` (prefixed with `ROLE_` internally) |
| `username` | string | Optional display username |

Example payload:

```json
{
  "userId": 42,
  "roles": ["STUDENT"],
  "username": "jane.doe"
}
```

Generate test tokens with the same secret used by your auth service.

## API Overview

### Student (JWT required)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/reviews` | Create review (enrollment check via Feign) |
| `PUT` | `/api/reviews/{reviewId}` | Update own review |
| `DELETE` | `/api/reviews/{reviewId}` | Delete own review |
| `GET` | `/api/reviews/my/course/{courseId}` | Get own review for course |

### Public (no auth)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/reviews/course/{courseId}` | Paginated ACTIVE reviews |
| `GET` | `/api/reviews/course/{courseId}/summary` | Rating summary (ACTIVE only) |

### Admin (`ROLE_ADMIN`)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/reviews` | All reviews (paginated) |
| `GET` | `/api/admin/reviews/{reviewId}` | Review details |
| `PATCH` | `/api/admin/reviews/{reviewId}/hide` | Hide review |
| `PATCH` | `/api/admin/reviews/{reviewId}/unhide` | Unhide review |
| `DELETE` | `/api/admin/reviews/{reviewId}` | Delete any review |

## Enrollment Integration

`EnrollmentClient` calls:

```
GET {ENROLLMENT_SERVICE_URL}/api/enrollments/check/{courseId}
```

Response: `{ "enrolled": true }`

The caller's `Authorization: Bearer <token>` header is forwarded. If the enrollment service is unavailable, the circuit breaker fallback returns `enrolled: false`.

## Student Name Resolution

Public listings use `StudentNameResolver`:

- If `USER_SERVICE_ENABLED=true`, calls `GET /api/users/{userId}` via Feign.
- Otherwise uses placeholder `Student {userId}` (see TODO in code).

## Build

```bash
mvn clean compile
mvn clean package
```

## Project Structure

```
review-service/
├── build.gradle
├── README.md
└── src/main/
    ├── java/com/lms/review/
    │   ├── ReviewServiceApplication.java
    │   ├── config/          # Security, OpenAPI, JPA, Feign
    │   ├── controller/      # Review & Admin controllers
    │   ├── dto/             # Request/response DTOs
    │   ├── entity/          # JPA entities
    │   ├── enums/           # ReviewStatus
    │   ├── exception/       # BusinessException, global handler
    │   ├── repository/      # Spring Data repositories
    │   ├── security/        # JWT filter & utilities
    │   ├── service/         # Business logic
    │   └── client/          # OpenFeign clients
    └── resources/
        └── application.yml
```
