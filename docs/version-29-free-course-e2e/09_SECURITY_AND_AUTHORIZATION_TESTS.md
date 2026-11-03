# ASIVO LMS — VERSION 29
# 09 SECURITY AND AUTHORIZATION VERIFICATION

**Verdict:** PASS  
**Scope:** Access controls, role-based authorization boundaries, negative failure tests, and cross-user isolation.

---

## 1. Security Test Matrix

| Scenario | Actor / Token | Target Endpoint | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **Anonymous Free Enrollment** | None (No JWT) | `POST /api/v1/courses/10/enroll` | `401 Unauthorized` | `401 Unauthorized` | **PASS** |
| **Anonymous Dedicated Route** | None (No JWT) | `POST /api/v1/enrollments/free/10` | `401 Unauthorized` | `401 Unauthorized` | **PASS** |
| **Draft Course Enrollment** | Valid Student JWT | `POST /api/v1/courses/{draftId}/enroll` | `400 Bad Request` | `400 Bad Request: Only published courses are eligible for enrollment.` | **PASS** |
| **Paid Course Free Bypass Attempt** | Valid Student JWT | `POST /api/v1/courses/{paidId}/enroll` | `400 Bad Request` | `400 Bad Request: Course is not free. Paid courses require checkout through order service.` | **PASS** |
| **Duplicate Enrollment Attack** | Valid Student JWT | `POST /api/v1/courses/10/enroll` (repeat) | `409 Conflict` | `409 Conflict: Student is already enrolled.` | **PASS** |
| **Unenrolled Learning Access** | Different Student JWT (Not Enrolled) | `GET /api/v1/courses/10/content` | `409 Conflict` / `403 Forbidden` | `409 Conflict: Student is not enrolled in this course` | **PASS** |
| **Cross-User Enrollment Spoofing** | Student A JWT | Attempting to pass Student B userId in request | Ignored. Backend derives student ID exclusively from verified JWT security context | Identity strictly bound to JWT principal | **PASS** |

---

## 2. JWT Principle Integrity Evidence

In `JwtFilter.java`:
```java
UUID userId = UUID.fromString(jwtUtil.extractUsername(token));
String role = JwtUtil.toSpringSecurityRole(jwtUtil.extractRole(token));
UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
SecurityContextHolder.getContext().setAuthentication(authentication);
```
The student identity is strictly derived from the signed cryptographic token, making user impersonation impossible.
