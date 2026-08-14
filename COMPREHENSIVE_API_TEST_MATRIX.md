# COMPREHENSIVE API TEST MATRIX

## LMS Microservices - Complete API Testing Matrix

**Total Endpoints to Test: 240**  
**Test Base URL:** http://localhost:8080  
**Test Date:** 2026-08-10

---

## Test Matrix Structure

| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|

---

## Test Status Legend
- **NOT_TESTED** - Endpoint has not been tested yet
- **PASS** - Test passed successfully
- **FAIL** - Test failed with error
- **SKIP** - Test skipped (requires specific setup/data)
- **WIP** - Work in progress

---

## Auth Required Legend
- **NONE** - No authentication required
- **BEARER** - Bearer token authentication
- **BASIC** - Basic authentication
- **S2S** - Service-to-service authentication
- **HEADER** - Custom header authentication

---

## 1. USER SERVICE (20 endpoints)

### UserController (8 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 1 | user-service | GET | `/api/v1/users/me` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 2 | user-service | PUT | `/api/v1/users/me` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 3 | user-service | POST | `/api/v1/users/me/photo` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 4 | user-service | DELETE | `/api/v1/users/me` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 5 | user-service | GET | `/api/v1/users` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 6 | user-service | GET | `/api/v1/users/{id}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 7 | user-service | PUT | `/api/v1/users/{id}/status` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 8 | user-service | DELETE | `/api/v1/users/{id}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

### RegistrationController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 9 | user-service | POST | `/api/v1/auth/register` | NONE | PUBLIC | PASS | 201 | User registered successfully | PASS | ✅ Tested successfully |
| 10 | user-service | POST | `/api/v1/auth/verify-email` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### UnifiedAuthenticationController (11 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 11 | user-service | POST | `/api/v1/auth/login` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 12 | user-service | POST | `/api/v1/auth/refresh` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 13 | user-service | POST | `/api/v1/auth/logout` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 14 | user-service | POST | `/api/v1/auth/login/otp/request` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 15 | user-service | POST | `/api/v1/auth/login/otp/verify` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 16 | user-service | POST | `/api/v1/auth/password/forgot` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 17 | user-service | POST | `/api/v1/auth/password/verify-otp` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 18 | user-service | POST | `/api/v1/auth/password/reset` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 19 | user-service | POST | `/api/v1/auth/change-password` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 20 | user-service | POST | `/api/v1/auth/switch-role` | BEARER | Any | NOT_TESTED | - | - | - | - |

### SessionController (3 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 21 | user-service | GET | `/api/v1/users/me/sessions` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 22 | user-service | DELETE | `/api/v1/users/me/sessions/{id}` | BEARER | Any | NOT_TESTED | - | - | - | - |
| 23 | user-service | DELETE | `/api/v1/users/me/sessions` | BEARER | Any | NOT_TESTED | - | - | - | - |

### InstructorController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 24 | user-service | POST | `/api/v1/instructors/applications` | BEARER | STUDENT | NOT_TESTED | - | - | - | - |
| 25 | user-service | GET | `/api/v1/instructors/applications/me` | BEARER | Any | NOT_TESTED | - | - | - | - |

### AdminInstructorController (4 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 26 | user-service | GET | `/api/v1/admin/instructors` | BEARER | MAIN_ADMIN,SUB_ADMIN | NOT_TESTED | - | - | - | - |
| 27 | user-service | GET | `/api/v1/admin/instructors/applications` | BEARER | MAIN_ADMIN,SUB_ADMIN | NOT_TESTED | - | - | - | - |
| 28 | user-service | PUT | `/api/v1/admin/instructors/applications/{userId}/approve` | BEARER | MAIN_ADMIN,SUB_ADMIN | NOT_TESTED | - | - | - | - |
| 29 | user-service | PUT | `/api/v1/admin/instructors/applications/{userId}/reject` | BEARER | MAIN_ADMIN,SUB_ADMIN | NOT_TESTED | - | - | - | - |

**User Service Subtotal: 20 endpoints**

---

## 2. ADMIN SERVICE (33 endpoints)

### AdminController (9 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 30 | admin-service | POST | `/api/v1/admin/register` | BEARER | MAIN_ADMIN | NOT_TESTED | - | - | - | - |
| 31 | admin-service | POST | `/api/v1/admin/login` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 32 | admin-service | POST | `/api/v1/admin/login/otp/request` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 33 | admin-service | POST | `/api/v1/admin/login/otp/verify` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 34 | admin-service | POST | `/api/v1/admin/password/forgot` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 35 | admin-service | POST | `/api/v1/admin/password/verify-otp` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 36 | admin-service | POST | `/api/v1/admin/password/reset` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 37 | admin-service | PUT | `/api/v1/admin/me` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 38 | admin-service | GET | `/api/v1/admin/me` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

### AdminCourseController (13 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 39 | admin-service | GET | `/api/v1/admin/courses` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 40 | admin-service | GET | `/api/v1/admin/courses/{courseId}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 41 | admin-service | GET | `/api/v1/admin/content/{courseId}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 42 | admin-service | PUT | `/api/v1/admin/courses/{courseId}/approve` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 43 | admin-service | PUT | `/api/v1/admin/courses/{courseId}/reject` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 44 | admin-service | DELETE | `/api/v1/admin/courses/{courseId}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 45 | admin-service | POST | `/api/v1/admin/courses/{courseId}/sections` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 46 | admin-service | DELETE | `/api/v1/admin/sections/{sectionId}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 47 | admin-service | POST | `/api/v1/admin/sections/{sectionId}/lectures` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 48 | admin-service | PUT | `/api/v1/admin/sections/{sectionId}/lectures/{lectureId}/approve` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 49 | admin-service | PUT | `/api/v1/admin/sections/{sectionId}/lectures/{lectureId}/reject` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 50 | admin-service | DELETE | `/api/v1/admin/sections/{sectionId}/lectures/{lectureId}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

### AdminUserController (8 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 51 | admin-service | GET | `/api/v1/admin/users` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 52 | admin-service | GET | `/api/v1/admin/users/{id}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 53 | admin-service | PUT | `/api/v1/admin/users/{id}/status` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 54 | admin-service | DELETE | `/api/v1/admin/users/{id}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 55 | admin-service | GET | `/api/v1/admin/instructors` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 56 | admin-service | GET | `/api/v1/admin/instructors/applications` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 57 | admin-service | PUT | `/api/v1/admin/instructors/applications/{userId}/approve` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 58 | admin-service | DELETE | `/api/v1/admin/instructors/{id}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

### AdminOrderController (4 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 59 | admin-service | GET | `/api/v1/admin/orders` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 60 | admin-service | GET | `/api/v1/admin/orders/{id}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 61 | admin-service | PUT | `/api/v1/admin/orders/{id}/status` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 62 | admin-service | POST | `/api/v1/admin/orders/{id}/refund` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

### AdminPaymentController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 63 | admin-service | GET | `/api/v1/admin/payments` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 64 | admin-service | GET | `/api/v1/admin/payments/{id}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

### AdminReportController (8 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 65 | admin-service | GET | `/api/v1/admin/reports/users` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 66 | admin-service | GET | `/api/v1/admin/reports/courses` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 67 | admin-service | GET | `/api/v1/admin/reports/revenue` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 68 | admin-service | GET | `/api/v1/admin/reports/orders` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 69 | admin-service | PUT | `/api/v1/admin/settings/platform` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 70 | admin-service | PUT | `/api/v1/admin/settings/payment` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 71 | admin-service | PUT | `/api/v1/admin/settings/notifications` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

### AdminReviewController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 72 | admin-service | GET | `/api/v1/admin/reviews` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 73 | admin-service | DELETE | `/api/v1/admin/reviews/{id}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

**Admin Service Subtotal: 33 endpoints**

---

## 3. COURSE SERVICE (20 endpoints)

### CourseController (9 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 74 | course-service | POST | `/api/v1/courses` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 75 | course-service | GET | `/api/v1/courses` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 76 | course-service | GET | `/api/v1/courses/{id}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 77 | course-service | PUT | `/api/v1/courses/{id}` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 78 | course-service | PATCH | `/api/v1/courses/{id}` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 79 | course-service | PATCH | `/api/v1/courses/{id}/status` | BEARER | ADMIN_SERVICE | NOT_TESTED | - | - | - | - |
| 80 | course-service | DELETE | `/api/v1/courses/{id}` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 81 | course-service | GET | `/api/v1/courses/{courseId}/students` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 82 | course-service | POST | `/api/v1/courses/{courseId}/enroll` | BEARER | STUDENT | NOT_TESTED | - | - | - | - |

### EnrollmentController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 83 | course-service | POST | `/api/v1/enrollments/internal/enroll` | S2S | SERVICE | NOT_TESTED | - | - | - | - |
| 84 | course-service | GET | `/api/v1/enrollments/check/{courseId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |

### CoursePreviewController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 85 | course-service | POST | `/api/v1/courses/{courseId}/preview` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 86 | course-service | GET | `/api/v1/courses/{courseId}/preview` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### LectureController (4 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 87 | course-service | POST | `/api/v1/sections/{sectionId}/lectures` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 88 | course-service | PATCH | `/api/v1/sections/{sectionId}/lectures/{lectureId}` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 89 | course-service | DELETE | `/api/v1/sections/{sectionId}/lectures/{lectureId}` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 90 | course-service | GET | `/api/v1/sections/{sectionId}/lectures` | BEARER | ENROLLED | NOT_TESTED | - | - | - | - |

### SectionController (4 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 91 | course-service | POST | `/api/v1/courses/{courseId}/sections` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 92 | course-service | GET | `/api/v1/courses/{courseId}/sections` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 93 | course-service | PATCH | `/api/v1/courses/sections/{sectionId}` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |
| 94 | course-service | DELETE | `/api/v1/courses/sections/{sectionId}` | BEARER | INSTRUCTOR,ADMIN | NOT_TESTED | - | - | - | - |

**Course Service Subtotal: 20 endpoints**

---

## 4. INSTRUCTOR SERVICE (16 endpoints)

### DashboardController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 95 | instructor-service | GET | `/api/v1/instructors/{id}/dashboard` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 96 | instructor-service | GET | `/api/v1/instructors/{id}/earnings` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |

### CourseController (9 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 97 | instructor-service | POST | `/api/v1/instructors/{id}/courses` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 98 | instructor-service | GET | `/api/v1/instructors/{id}/courses` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 99 | instructor-service | GET | `/api/v1/instructors/{id}/courses/{courseId}` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 100 | instructor-service | PUT | `/api/v1/instructors/{id}/courses/{courseId}` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 101 | instructor-service | DELETE | `/api/v1/instructors/{id}/courses/{courseId}` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 102 | instructor-service | GET | `/api/v1/instructors/{id}/courses/{courseId}/students` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 103 | instructor-service | GET | `/api/v1/instructors/{id}/courses/{courseId}/students/{studentId}` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 104 | instructor-service | POST | `/api/v1/instructors/{id}/courses/{courseId}/grades` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 105 | instructor-service | GET | `/api/v1/instructors/{id}/courses/{courseId}/analytics` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |

### ContentController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 106 | instructor-service | PATCH | `/api/v1/instructors/{instructorId}/content/{contentId}/publish` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |

### ModuleController (4 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 107 | instructor-service | POST | `/api/v1/instructors/{instructorId}/courses/{courseId}/modules` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 108 | instructor-service | PUT | `/api/v1/instructors/{instructorId}/courses/{courseId}/modules/{moduleId}` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 109 | instructor-service | DELETE | `/api/v1/instructors/{instructorId}/courses/{courseId}/modules/{moduleId}` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 110 | instructor-service | POST | `/api/v1/instructors/{instructorId}/courses/{courseId}/modules/upload` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |

### AnnouncementController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 111 | instructor-service | POST | `/api/v1/instructors/{id}/courses/{courseId}/announcements` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |

### CourseMessageController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 112 | instructor-service | POST | `/api/v1/instructors/{id}/courses/{courseId}/messages` | BEARER | INSTRUCTOR | NOT_TESTED | - | - | - | - |

### PublicCourseController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 113 | instructor-service | GET | `/api/v1/courses/{courseId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

**Instructor Service Subtotal: 16 endpoints**

---

## 5. CART SERVICE (9 endpoints)

### CartController (9 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 114 | cart-service | POST | `/api/v1/cart` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 115 | cart-service | GET | `/api/v1/cart` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 116 | cart-service | GET | `/api/v1/cart/summary` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 117 | cart-service | PUT | `/api/v1/cart/coupon` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 118 | cart-service | DELETE | `/api/v1/cart/coupon` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 119 | cart-service | DELETE | `/api/v1/cart/{courseId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 120 | cart-service | DELETE | `/api/v1/cart` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 121 | cart-service | POST | `/api/v1/cart/checkout` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 122 | cart-service | GET | `/api/v1/cart/internal/{userId}` | S2S | SERVICE | NOT_TESTED | - | - | - | - |

**Cart Service Subtotal: 9 endpoints**

---

## 6. COUPON SERVICE (15 endpoints)

### CouponController (15 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 123 | coupon-service | POST | `/api/v1/coupons` | BEARER | ADMIN,INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 124 | coupon-service | POST | `/api/v1/coupons/bulk` | BEARER | ADMIN,INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 125 | coupon-service | GET | `/api/v1/coupons` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 126 | coupon-service | GET | `/api/v1/coupons/{couponId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 127 | coupon-service | GET | `/api/v1/coupons/validate/{code}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 128 | coupon-service | PUT | `/api/v1/coupons/{couponId}` | BEARER | ADMIN,INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 129 | coupon-service | DELETE | `/api/v1/coupons/{couponId}` | BEARER | ADMIN,INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 130 | coupon-service | PATCH | `/api/v1/coupons/{couponId}/activate` | BEARER | ADMIN,INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 131 | coupon-service | PATCH | `/api/v1/coupons/{couponId}/deactivate` | BEARER | ADMIN,INSTRUCTOR | NOT_TESTED | - | - | - | - |
| 132 | coupon-service | GET | `/api/v1/coupons/my` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 133 | coupon-service | GET | `/api/v1/coupons/campaigns` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 134 | coupon-service | POST | `/api/v1/coupons/validate` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 135 | coupon-service | POST | `/api/v1/coupons/best-discount` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 136 | coupon-service | POST | `/api/v1/coupons/redeem` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 137 | coupon-service | POST | `/api/v1/coupons/assign-user` | BEARER | ADMIN,INSTRUCTOR | NOT_TESTED | - | - | - | - |

**Coupon Service Subtotal: 15 endpoints**

---

## 7. ORDER SERVICE (7 endpoints)

### OrderController (7 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 138 | order-service | POST | `/api/v1/orders/create` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 139 | order-service | GET | `/api/v1/orders/{orderId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 140 | order-service | GET | `/api/v1/orders` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 141 | order-service | GET | `/api/v1/orders/user/{userId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 142 | order-service | PUT | `/api/v1/orders/{orderId}/status` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 143 | order-service | DELETE | `/api/v1/orders/{orderId}/cancel` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 144 | order-service | POST | `/api/v1/orders/{orderId}/refund` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

**Order Service Subtotal: 7 endpoints**

---

## 8. PAYMENT SERVICE (21 endpoints)

### PaymentController (18 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 145 | payment-service | GET | `/api/v1/payments` | BEARER | ADMIN_SERVICE | NOT_TESTED | - | - | - | - |
| 146 | payment-service | POST | `/api/v1/payments/create` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 147 | payment-service | POST | `/api/v1/payments/create-and-pay` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 148 | payment-service | GET | `/api/v1/payments/checkout/{txnId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 149 | payment-service | GET | `/api/v1/payments/callback/success` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 150 | payment-service | GET | `/api/v1/payments/callback/failure` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 151 | payment-service | POST | `/api/v1/payments/callback/success` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 152 | payment-service | POST | `/api/v1/payments/callback/failure` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 153 | payment-service | POST | `/api/v1/payments/verify` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 154 | payment-service | POST | `/api/v1/payments/payu/verify` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 155 | payment-service | POST | `/api/v1/payments/payu/check-payment` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 156 | payment-service | POST | `/api/v1/payments/payu/tdr` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 157 | payment-service | POST | `/api/v1/payments/payu/transaction-details` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 158 | payment-service | POST | `/api/v1/payments/payu/payment-links` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 159 | payment-service | POST | `/api/v1/payments/payu/consent-checkout` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 160 | payment-service | POST | `/api/v1/payments/refund` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 161 | payment-service | GET | `/api/v1/payments/instructor/{instructorId}/courses/{courseId}/payments` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 162 | payment-service | GET | `/api/v1/payments/{txnId}/refund-status` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### TestCourseController (3 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 163 | payment-service | POST | `/api/v1/test/courses` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 164 | payment-service | GET | `/api/v1/test/courses` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 165 | payment-service | GET | `/api/v1/test/courses/{courseId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

**Payment Service Subtotal: 21 endpoints**

---

## 9. REVIEW SERVICE (10 endpoints)

### ReviewController (7 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 166 | review-service | POST | `/api/v1/reviews` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 167 | review-service | PUT | `/api/v1/reviews/{reviewId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 168 | review-service | DELETE | `/api/v1/reviews/{reviewId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 169 | review-service | GET | `/api/v1/reviews/{reviewId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 170 | review-service | GET | `/api/v1/reviews/my/course/{courseId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 171 | review-service | GET | `/api/v1/reviews/course/{courseId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 172 | review-service | GET | `/api/v1/reviews/course/{courseId}/summary` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### AdminReviewController (3 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 173 | review-service | GET | `/api/v1/admin/reviews` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 174 | review-service | GET | `/api/v1/admin/reviews/{reviewUuid}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 175 | review-service | PATCH | `/api/v1/admin/reviews/{reviewUuid}/hide` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 176 | review-service | PATCH | `/api/v1/admin/reviews/{reviewUuid}/unhide` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 177 | review-service | DELETE | `/api/v1/admin/reviews/{reviewUuid}` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

**Review Service Subtotal: 10 endpoints**

---

## 10. NOTIFICATION SERVICE (68 endpoints)

### NotificationController (9 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 178 | notification-service | POST | `/api/v1/notifications` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 179 | notification-service | POST | `/api/v1/notifications/bulk` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 180 | notification-service | GET | `/api/v1/notifications/{id}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 181 | notification-service | GET | `/api/v1/notifications` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 182 | notification-service | GET | `/api/v1/notifications/user/{userId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 183 | notification-service | GET | `/api/v1/notifications/user/{userId}/combined` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 184 | notification-service | PUT | `/api/v1/notifications/{id}/read` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 185 | notification-service | DELETE | `/api/v1/notifications/{id}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 186 | notification-service | POST | `/api/v1/notifications/{id}/retry` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### AdminNotificationController (4 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 187 | notification-service | POST | `/api/v1/admin/broadcast` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 188 | notification-service | POST | `/api/v1/admin/reprocess-dlq` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 189 | notification-service | GET | `/api/v1/admin/system-health` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |
| 190 | notification-service | PUT | `/api/v1/admin/settings/notifications` | BEARER | ADMIN | NOT_TESTED | - | - | - | - |

### UserNotificationController (5 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 191 | notification-service | GET | `/api/v1/users/me/notifications` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 192 | notification-service | GET | `/api/v1/users/me/unread` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 193 | notification-service | PUT | `/api/v1/users/me/read/{id}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 194 | notification-service | PUT | `/api/v1/users/me/read-all` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 195 | notification-service | GET | `/api/v1/users/me/count` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |

### InstructorNotificationController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 196 | notification-service | POST | `/api/v1/notifications/instructor/{instructorId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### CourseNotificationController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 197 | notification-service | POST | `/api/v1/notifications/course/{courseId}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### AnnouncementController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 198 | notification-service | POST | `/api/v1/announcements` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 199 | notification-service | GET | `/api/v1/announcements/course/{courseId}/detailed` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### DeviceTokenController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 200 | notification-service | POST | `/api/v1/device-tokens` | HEADER | X-USER-ID | NOT_TESTED | - | - | - | - |

### PushNotificationController (3 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 201 | notification-service | POST | `/api/v1/push/send` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 202 | notification-service | POST | `/api/v1/push/topic` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 203 | notification-service | POST | `/api/v1/push/broadcast` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### SystemController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 204 | notification-service | GET | `/api/v1/system/health` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### TemplateController (4 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 205 | notification-service | POST | `/api/v1/templates` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 206 | notification-service | GET | `/api/v1/templates` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 207 | notification-service | PUT | `/api/v1/templates/{id}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 208 | notification-service | DELETE | `/api/v1/templates/{id}` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### PreferenceController (2 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 209 | notification-service | GET | `/api/v1/preferences` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |
| 210 | notification-service | PUT | `/api/v1/preferences` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### AssignmentReminderController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 211 | notification-service | POST | `/api/v1/assignments/reminder` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### CertificateNotifyController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 212 | notification-service | POST | `/api/v1/certificates/notify` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

### LiveClassReminderController (1 endpoint)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 213 | notification-service | POST | `/api/v1/live-class/reminder` | NONE | PUBLIC | NOT_TESTED | - | - | - | - |

**Notification Service Subtotal: 68 endpoints**

---

## 11. WISHLIST SERVICE (7 endpoints)

### WishlistController (7 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 214 | wishlist-service | POST | `/api/v1/wishlist` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 215 | wishlist-service | GET | `/api/v1/wishlist` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 216 | wishlist-service | GET | `/api/v1/wishlist/{wishlistId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 217 | wishlist-service | GET | `/api/v1/wishlist/check/{courseId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 218 | wishlist-service | POST | `/api/v1/wishlist/{courseId}/move-to-cart` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 219 | wishlist-service | DELETE | `/api/v1/wishlist/{courseId}` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |
| 220 | wishlist-service | DELETE | `/api/v1/wishlist` | BEARER | AUTHENTICATED | NOT_TESTED | - | - | - | - |

**Wishlist Service Subtotal: 7 endpoints**

---

## 12. API GATEWAY (4 endpoints)

### SessionManagementController (4 endpoints)
| # | Service | Method | Endpoint | Auth Required | Role Required | Test Status | HTTP Status | Response | Result | Notes |
|---|---------|--------|----------|---------------|---------------|-------------|-------------|----------|--------|-------|
| 221 | api-gateway | GET | `/api/sessions` | HEADER | X-USER-ID,X-SESSION-ID | NOT_TESTED | - | - | - | - |
| 222 | api-gateway | DELETE | `/api/sessions/{sessionId}` | HEADER | X-USER-ID,X-SESSION-ID | NOT_TESTED | - | - | - | - |
| 223 | api-gateway | POST | `/api/sessions/deactivate-others` | HEADER | X-USER-ID,X-SESSION-ID | NOT_TESTED | - | - | - | - |
| 224 | api-gateway | POST | `/api/sessions/deactivate-all` | HEADER | X-USER-ID | NOT_TESTED | - | - | - | - |

**API Gateway Subtotal: 4 endpoints**

---

## SUMMARY STATISTICS

| Service | Total Endpoints | Tested | Passed | Failed | Skipped | Not Tested |
|---------|------------------|--------|--------|--------|---------|-------------|
| User Service | 20 | 1 | 1 | 0 | 0 | 19 |
| Admin Service | 33 | 0 | 0 | 0 | 0 | 33 |
| Course Service | 20 | 0 | 0 | 0 | 0 | 20 |
| Instructor Service | 16 | 0 | 0 | 0 | 0 | 16 |
| Cart Service | 9 | 0 | 0 | 0 | 0 | 9 |
| Coupon Service | 15 | 0 | 0 | 0 | 0 | 15 |
| Order Service | 7 | 0 | 0 | 0 | 0 | 7 |
| Payment Service | 21 | 0 | 0 | 0 | 0 | 21 |
| Review Service | 10 | 0 | 0 | 0 | 0 | 10 |
| Notification Service | 68 | 0 | 0 | 0 | 0 | 68 |
| Wishlist Service | 7 | 0 | 0 | 0 | 0 | 7 |
| API Gateway | 4 | 0 | 0 | 0 | 0 | 4 |
| **TOTAL** | **240** | **1** | **1** | **0** | **0** | **239** |

---

## TESTING PROGRESS

**Overall Progress: 0.4% (1/240 endpoints tested)**

This test matrix will be updated as each endpoint is tested through the API gateway at http://localhost:8080.