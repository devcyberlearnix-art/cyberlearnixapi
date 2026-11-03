# ASIVO LMS — VERSION 29
# 02 BACKEND API INVENTORY: FREE COURSE LIFECYCLE

**Verdict:** PASS  
**Documentation:** Complete inventory of reused and verified REST endpoints powering the free course workflow.

---

## 1. Course Management Endpoints

| Method | Endpoint | Authorized Roles | Service | Description | Status |
|---|---|---|---|---|---|
| `POST` | `/api/v1/courses` | `INSTRUCTOR`, `MAIN_ADMIN`, `SUB_ADMIN` | `course-service` | Create course with metadata, price = 0, status = DRAFT | **PASS** |
| `GET` | `/api/v1/courses/list` | Anonymous / Public | `course-service` | Paginated catalogue with `free=true`, categories, levels | **PASS** |
| `GET` | `/api/v1/courses/{id}` | Anonymous / Public | `course-service` | Public course details, sections, ratings, and pricing | **PASS** |
| `PATCH` | `/api/v1/courses/{id}/status` | `MAIN_ADMIN`, `SUB_ADMIN`, `INSTRUCTOR` | `course-service` | Update course publication status (DRAFT $\rightarrow$ PUBLISHED) | **PASS** |
| `PUT` | `/api/v1/courses/{id}` | `INSTRUCTOR`, `MAIN_ADMIN` | `course-service` | Full update of course properties | **PASS** |
| `DELETE` | `/api/v1/courses/{id}` | `INSTRUCTOR`, `MAIN_ADMIN` | `course-service` | Soft/hard delete of course entity | **PASS** |

---

## 2. Curriculum Endpoints (Sections & Lectures)

| Method | Endpoint | Authorized Roles | Service | Description | Status |
|---|---|---|---|---|---|
| `POST` | `/api/v1/courses/{courseId}/sections` | `INSTRUCTOR`, `MAIN_ADMIN` | `course-service` | Create new section with orderIndex | **PASS** |
| `GET` | `/api/v1/courses/{courseId}/sections` | Anonymous / Public | `course-service` | Public curriculum outline | **PASS** |
| `POST` | `/api/v1/sections/{sectionId}/lectures` | `INSTRUCTOR`, `MAIN_ADMIN` | `course-service` | Add video/article lecture to section | **PASS** |
| `GET` | `/api/v1/sections/{sectionId}/lectures` | Enrolled / Public Preview | `course-service` | Fetch section lectures | **PASS** |
| `PATCH` | `/api/v1/sections/{sectionId}/lectures/{id}` | `INSTRUCTOR`, `MAIN_ADMIN` | `course-service` | Edit lecture metadata | **PASS** |

---

## 3. Enrollment Endpoints

| Method | Endpoint | Authorized Roles | Service | Description | Status |
|---|---|---|---|---|---|
| `POST` | `/api/v1/courses/{courseId}/enroll` | `STUDENT`, `USER`, `INSTRUCTOR`, `ADMIN` | `course-service` | Authenticated free course enrollment | **PASS** |
| `POST` | `/api/v1/enrollments/free/{courseId}` | `STUDENT`, `USER`, `INSTRUCTOR`, `ADMIN` | `course-service` | Dedicated free course enrollment route | **PASS** |
| `GET` | `/api/v1/enrollments/check/{courseId}` | Authenticated (`STUDENT`, etc.) | `course-service` | Check if current student is enrolled | **PASS** |
| `GET` | `/api/v1/enrollments/users/{userId}` | Authenticated / Admin | `course-service` | Enumerate all courses enrolled by user | **PASS** |
| `GET` | `/api/v1/courses/{courseId}/content` | Enrolled Students Only | `course-service` | Full curriculum & non-preview media access | **PASS** |

---

## 4. Student Dashboard & Progress Endpoints

| Method | Endpoint | Authorized Roles | Service | Description | Status |
|---|---|---|---|---|---|
| `GET` | `/api/v1/dashboard/student/courses` | `STUDENT`, `USER` | `course-service` | Enrolled courses with active completion progress | **PASS** |
| `POST` | `/api/v1/progress/lectures/{id}/start` | Authenticated (`STUDENT`) | `course-service` | Initialize lecture learning attempt | **PASS** |
| `PATCH` | `/api/v1/progress/lectures/{id}` | Authenticated (`STUDENT`) | `course-service` | Update progress percentage & time spent | **PASS** |
| `PATCH` | `/api/v1/progress/lectures/{id}/complete` | Authenticated (`STUDENT`) | `course-service` | Mark lecture 100% completed | **PASS** |
| `GET` | `/api/v1/progress/courses/{courseId}` | Authenticated (`STUDENT`) | `course-service` | Aggregated progress for enrolled track | **PASS** |
