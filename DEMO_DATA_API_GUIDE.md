# CyberLearnix Demo Data API Guide

Base URL: `http://localhost:8080`

## Demo Accounts

All demo accounts use password `Demo@12345`.

| Role | Email | User ID |
|---|---|---|
| Student | `demo.student1@cyberlearnix.com` | `3be5c653-b22c-4121-adf6-79f971ac7252` |
| Student | `demo.student2@cyberlearnix.com` | `2ecf53e1-3e62-456e-84db-74d754312d78` |
| Student | `demo.student3@cyberlearnix.com` | `2d9d7c10-ea3f-4d12-91fa-8f5307609413` |
| Instructor | `demo.instructor1@cyberlearnix.com` | `b00b333c-7788-4c70-a239-6ff18c34a3b4` |
| Instructor | `demo.instructor2@cyberlearnix.com` | `3e2bbbfd-b436-48a2-922a-84142cf93b5d` |

Main admin: `mainadmin@cyberlearnix.com` / `MainAdmin@123`.

## Authentication

`POST /api/v1/auth/login`

```json
{
  "email": "demo.student1@cyberlearnix.com",
  "password": "Demo@12345"
}
```

The current response is not wrapped in `data`. Read the token and user from:

```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": "3be5c653-b22c-4121-adf6-79f971ac7252",
    "email": "demo.student1@cyberlearnix.com",
    "role": "STUDENT"
  },
  "authentication": {
    "accessToken": "<jwt>",
    "accessTokenExpiresIn": "15 minutes",
    "refreshToken": "<jwt>",
    "refreshTokenExpiresIn": "30 days"
  }
}
```

Send protected requests with `Authorization: Bearer <authentication.accessToken>`.

## Student APIs

- `GET /api/v1/users/me` returns the authenticated student profile.
- `POST /api/v1/courses/2/enroll` enrolls a student in the free Java course.
- Enrollment takes no request body.

Successful enrollment response:

```json
{
  "success": true,
  "message": "Student enrolled in the course successfully."
}
```

Student 1 is already enrolled in course `2`.

## Public Course APIs

- `GET /api/v1/courses` returns the public course array directly.
- `GET /api/v1/courses/{courseId}` returns one course directly.

| Public course ID | Title | Instructor user ID | Price | Status |
|---|---|---|---:|---|
| 1 | Spring Boot Microservices Mastery | `b00b333c-7788-4c70-a239-6ff18c34a3b4` | 2499.00 | PUBLISHED |
| 2 | Java Foundations for Beginners | `b00b333c-7788-4c70-a239-6ff18c34a3b4` | 0.00 | PUBLISHED |
| 3 | React and TypeScript in Practice | `3e2bbbfd-b436-48a2-922a-84142cf93b5d` | 1999.00 | PUBLISHED |
| 4 | Modern UI Design Systems | `3e2bbbfd-b436-48a2-922a-84142cf93b5d` | 1499.00 | PUBLISHED |

Example catalog item:

```json
{
  "id": 1,
  "title": "Spring Boot Microservices Mastery",
  "status": "PUBLISHED",
  "instructorId": "b00b333c-7788-4c70-a239-6ff18c34a3b4",
  "price": 2499.00
}
```

## Instructor APIs

Use an instructor access token and the instructor's user ID in the path.

- `GET /api/v1/instructors/{instructorUserId}/courses`
- `GET /api/v1/instructors/{instructorUserId}/courses/{localCourseId}`
- `POST /api/v1/instructors/{instructorUserId}/courses`
- `PUT /api/v1/instructors/{instructorUserId}/courses/{localCourseId}`
- `DELETE /api/v1/instructors/{instructorUserId}/courses/{localCourseId}`

Course creation request:

```json
{
  "title": "API Design with Spring",
  "subtitle": "Build consistent REST APIs",
  "description": "A practical API design course.",
  "price": 999.00,
  "category": "Backend Development",
  "status": "PUBLISHED",
  "tags": ["Java", "Spring Boot", "REST"],
  "thumbnailUrl": "https://example.com/course.jpg",
  "previewVideoUrl": "https://example.com/preview.mp4"
}
```

Instructor course responses use instructor-service local IDs. Current local IDs are `5`, `6`, `7`, and `8`; public catalog IDs are `1`, `2`, `3`, and `4`.

## Admin APIs

Use the main-admin access token.

- `GET /api/v1/admin/instructors`
- `GET /api/v1/admin/instructors/applications`
- `PUT /api/v1/admin/instructors/applications/{userId}/approve`
- `PUT /api/v1/admin/instructors/applications/{userId}/reject`

The instructor list is wrapped as:

```json
{
  "success": true,
  "data": {
    "totalUsers": 2,
    "users": [
      {
        "id": "b00b333c-7788-4c70-a239-6ff18c34a3b4",
        "email": "demo.instructor1@cyberlearnix.com",
        "role": "INSTRUCTOR",
        "status": "ACTIVE"
      }
    ]
  }
}
```

## Frontend Rules

1. Use `authentication.accessToken` from login; do not read `data.accessToken`.
2. Treat public course responses as a direct array, not an `ApiResponse.data` array.
3. Use UUID strings for user and instructor identifiers.
4. Use the public numeric course ID for catalog, enrollment, cart, order, and review flows.
5. Use the instructor local course ID only for `/api/v1/instructors/{id}/courses/**` routes.