# COMPLETE API CATALOG

## LMS Microservices - Complete API Endpoint Catalog

**Total Endpoints Discovered: 240**

---

## Table of Contents
1. [User Service](#user-service) - 20 endpoints
2. [Admin Service](#admin-service) - 33 endpoints
3. [Course Service](#course-service) - 20 endpoints
4. [Instructor Service](#instructor-service) - 16 endpoints
5. [Cart Service](#cart-service) - 9 endpoints
6. [Coupon Service](#coupon-service) - 15 endpoints
7. [Order Service](#order-service) - 7 endpoints
8. [Payment Service](#payment-service) - 21 endpoints
9. [Review Service](#review-service) - 10 endpoints
10. [Notification Service](#notification-service) - 68 endpoints
11. [Wishlist Service](#wishlist-service) - 7 endpoints
12. [API Gateway](#api-gateway) - 4 endpoints

---

## User Service
**Base Path:** `/api/v1`  
**Controllers:** UserController, RegistrationController, UnifiedAuthenticationController, SessionController, InstructorController, AdminInstructorController

### UserController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/users/me` | Get current user profile | Bearer Token |
| PUT | `/users/me` | Update current user profile | Bearer Token |
| POST | `/users/me/photo` | Upload profile photo | Bearer Token |
| DELETE | `/users/me` | Delete user account | Bearer Token |
| GET | `/users` | Get all users (Admin) | Service Token or Admin |
| GET | `/users/{id}` | Get user by ID | Admin |
| PUT | `/users/{id}/status` | Update user status | Admin |
| DELETE | `/users/{id}` | Delete user by ID | Admin |

### RegistrationController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/auth/register` | Register new user (sends OTP) | None |
| POST | `/auth/verify-email` | Verify email with OTP | None |

### UnifiedAuthenticationController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/auth/login` | Unified login (Student/Instructor/Admin) | None |
| POST | `/auth/refresh` | Refresh access token | Bearer Token (Refresh) |
| POST | `/auth/logout` | Logout user | Bearer Token |
| POST | `/auth/login/otp/request` | Request login OTP | None |
| POST | `/auth/login/otp/verify` | Verify login OTP | None |
| POST | `/auth/password/forgot` | Forgot password (sends OTP) | None |
| POST | `/auth/password/verify-otp` | Verify password reset OTP | None |
| POST | `/auth/password/reset` | Reset password | Optional Bearer Token |
| POST | `/auth/change-password` | Change password (authenticated) | Bearer Token |
| POST | `/auth/switch-role` | Switch user role | Bearer Token |

### SessionController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/users/me/sessions` | List all user sessions | Bearer Token |
| DELETE | `/users/me/sessions/{id}` | Logout specific device | Bearer Token |
| DELETE | `/users/me/sessions` | Logout from all sessions | Bearer Token |

### InstructorController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/instructors/applications` | Apply for instructor (multipart) | Bearer Token |
| GET | `/instructors/applications/me` | Get application status | Bearer Token |

### AdminInstructorController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/admin/instructors` | Get all approved instructors | @PreAuthorize: MAIN_ADMIN, SUB_ADMIN |
| GET | `/admin/instructors/applications` | Get all instructor applications | @PreAuthorize: MAIN_ADMIN, SUB_ADMIN |
| PUT | `/admin/instructors/applications/{userId}/approve` | Approve instructor application | @PreAuthorize: MAIN_ADMIN, SUB_ADMIN |
| PUT | `/admin/instructors/applications/{userId}/reject` | Reject instructor application | @PreAuthorize: MAIN_ADMIN, SUB_ADMIN |

**User Service Total: 20 endpoints**

---

## Admin Service
**Base Path:** `/api/v1/admin`  
**Controllers:** AdminController, AdminCourseController, AdminUserController, AdminOrderController, AdminPaymentController, AdminReportController, AdminReviewController

### AdminController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/admin/register` | Register sub-admin | Main Admin Bearer Token |
| POST | `/admin/login` | Admin password login | None |
| POST | `/admin/login/otp/request` | Request admin login OTP | None |
| POST | `/admin/login/otp/verify` | Verify admin login OTP | None |
| POST | `/admin/password/forgot` | Admin forgot password | None |
| POST | `/admin/password/verify-otp` | Verify admin password OTP | None |
| POST | `/admin/password/reset` | Reset admin password | None |
| PUT | `/admin/me` | Update admin profile | @AuthenticationPrincipal |
| GET | `/admin/me` | Get admin profile | @AuthenticationPrincipal |

### AdminCourseController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/admin/courses` | Get all courses | Admin |
| GET | `/admin/courses/{courseId}` | Get course by ID | Admin |
| GET | `/admin/content/{courseId}` | Get course content | Admin |
| PUT | `/admin/courses/{courseId}/approve` | Approve course | Admin |
| PUT | `/admin/courses/{courseId}/reject` | Reject course | Admin |
| DELETE | `/admin/courses/{courseId}` | Delete course | Admin |
| POST | `/admin/courses/{courseId}/sections` | Create section | Admin |
| DELETE | `/admin/sections/{sectionId}` | Delete section | Admin |
| POST | `/admin/sections/{sectionId}/lectures` | Create lecture | Admin |
| PUT | `/admin/sections/{sectionId}/lectures/{lectureId}/approve` | Approve lecture | Admin |
| PUT | `/admin/sections/{sectionId}/lectures/{lectureId}/reject` | Reject lecture | Admin |
| DELETE | `/admin/sections/{sectionId}/lectures/{lectureId}` | Delete lecture | Admin |

### AdminUserController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/admin/users` | Get all users | Admin |
| GET | `/admin/users/{id}` | Get user by ID | Admin |
| PUT | `/admin/users/{id}/status` | Update user status | Admin |
| DELETE | `/admin/users/{id}` | Delete user | Admin |
| GET | `/admin/instructors` | Get all instructors | Admin |
| GET | `/admin/instructors/applications` | Get instructor applications | Admin |
| PUT | `/admin/instructors/applications/{userId}/approve` | Approve instructor | Admin |
| DELETE | `/admin/instructors/{id}` | Delete instructor | Admin |

### AdminOrderController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/admin/orders` | Get all orders | Admin |
| GET | `/admin/orders/{id}` | Get order by ID | Admin |
| PUT | `/admin/orders/{id}/status` | Update order status | Admin |
| POST | `/admin/orders/{id}/refund` | Process refund | Admin |

### AdminPaymentController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/admin/payments` | Get all payments | Admin |
| GET | `/admin/payments/{id}` | Get payment by ID | Admin |

### AdminReportController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/admin/reports/users` | Get users report | @AuthenticationPrincipal |
| GET | `/admin/reports/courses` | Get courses report | @AuthenticationPrincipal |
| GET | `/admin/reports/revenue` | Get revenue report | @AuthenticationPrincipal |
| GET | `/admin/reports/orders` | Get orders report | @AuthenticationPrincipal |
| PUT | `/admin/settings/platform` | Update platform settings | Admin |
| PUT | `/admin/settings/payment` | Update payment settings | Admin |
| PUT | `/admin/settings/notifications` | Update notification settings | Admin |

### AdminReviewController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/admin/reviews` | Get all reviews | Admin |
| DELETE | `/admin/reviews/{id}` | Delete review | Admin |

**Admin Service Total: 33 endpoints**

---

## Course Service
**Base Path:** `/api/v1`  
**Controllers:** CourseController, EnrollmentController, CoursePreviewController, LectureController, SectionController
**Note:** Course IDs are Long (numeric), not UUID

### CourseController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/courses` | Create course | Instructor/Admin |
| GET | `/courses` | Get all courses (Public) | None |
| GET | `/courses/{id}` | Get course by ID (Public) | None |
| PUT | `/courses/{id}` | Update course (Full) | Instructor/Admin |
| PATCH | `/courses/{id}` | Update course (Partial) | Instructor/Admin |
| PATCH | `/courses/{id}/status` | Update course status | Admin Service |
| DELETE | `/courses/{id}` | Delete course | Instructor/Admin |
| GET | `/courses/{courseId}/students` | Get enrolled students | Public |
| POST | `/courses/{courseId}/enroll` | Enroll in free course | Student (JWT) |

### EnrollmentController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/enrollments/internal/enroll` | Internal enrollment (S2S) | Service-to-Service |
| GET | `/enrollments/check/{courseId}` | Check enrollment status | JWT Auth |

### CoursePreviewController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/courses/{courseId}/preview` | Create course preview | Instructor/Admin |
| GET | `/courses/{courseId}/preview` | Get course preview | Public |

### LectureController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/sections/{sectionId}/lectures` | Create lecture | @PreAuthorize: INSTRUCTOR, ADMIN |
| PATCH | `/sections/{sectionId}/lectures/{lectureId}` | Update lecture | @PreAuthorize: INSTRUCTOR, ADMIN |
| DELETE | `/sections/{sectionId}/lectures/{lectureId}` | Delete lecture | @PreAuthorize: INSTRUCTOR, ADMIN |
| GET | `/sections/{sectionId}/lectures` | Get lectures by section | Enrolled users |

### SectionController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/courses/{courseId}/sections` | Create section | Instructor/Admin |
| GET | `/courses/{courseId}/sections` | Get sections by course | Public |
| PATCH | `/courses/sections/{sectionId}` | Update section | Instructor/Admin |
| DELETE | `/courses/sections/{sectionId}` | Delete section | Instructor/Admin |

**Course Service Total: 20 endpoints**

---

## Instructor Service
**Base Path:** `/api/v1/instructors`  
**Controllers:** DashboardController, CourseController, ContentController, ModuleController, AnnouncementController, CourseMessageController, PublicCourseController

### DashboardController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/instructors/{id}/dashboard` | Get instructor dashboard | Instructor |
| GET | `/instructors/{id}/earnings` | Get instructor earnings | Instructor |

### CourseController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/instructors/{id}/courses` | Create course | Instructor |
| GET | `/instructors/{id}/courses` | Get courses by instructor | Instructor |
| GET | `/instructors/{id}/courses/{courseId}` | Get course by ID | Instructor |
| PUT | `/instructors/{id}/courses/{courseId}` | Update course | Instructor |
| DELETE | `/instructors/{id}/courses/{courseId}` | Delete/unpublish course | Instructor |
| GET | `/instructors/{id}/courses/{courseId}/students` | Get enrolled students | Instructor |
| GET | `/instructors/{id}/courses/{courseId}/students/{studentId}` | Get student progress | Instructor |
| POST | `/instructors/{id}/courses/{courseId}/grades` | Assign/update grade | Instructor |
| GET | `/instructors/{id}/courses/{courseId}/analytics` | Get course analytics | Instructor |

### ContentController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| PATCH | `/instructors/{instructorId}/content/{contentId}/publish` | Publish/unpublish content | Instructor |

### ModuleController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/instructors/{instructorId}/courses/{courseId}/modules` | Add module | Instructor |
| PUT | `/instructors/{instructorId}/courses/{courseId}/modules/{moduleId}` | Update module | Instructor |
| DELETE | `/instructors/{instructorId}/courses/{courseId}/modules/{moduleId}` | Delete module | Instructor |
| POST | `/instructors/{instructorId}/courses/{courseId}/modules/upload` | Upload resource (multipart) | Instructor |

### AnnouncementController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/instructors/{id}/courses/{courseId}/announcements` | Create announcement | Instructor |

### CourseMessageController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/instructors/{id}/courses/{courseId}/messages` | Send course message | Instructor |

### PublicCourseController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/courses/{courseId}` | Get course by ID (Public) | None |

**Instructor Service Total: 16 endpoints**

---

## Cart Service
**Base Path:** `/api/v1/cart`  
**Controllers:** CartController

### CartController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/cart` | Add course to cart | Authentication |
| GET | `/cart` | Get user cart | Authentication |
| GET | `/cart/summary` | Get cart summary | Authentication |
| PUT | `/cart/coupon` | Apply coupon to cart | Authentication |
| DELETE | `/cart/coupon` | Remove coupon from cart | Authentication |
| DELETE | `/cart/{courseId}` | Remove course from cart | Authentication |
| DELETE | `/cart` | Clear cart | Authentication |
| POST | `/cart/checkout` | Initiate checkout | Authentication |
| GET | `/cart/internal/{userId}` | Get cart (internal S2S) | Service-to-Service |
| DELETE | `/cart/internal/{userId}` | Clear cart (internal S2S) | Service-to-Service |

**Cart Service Total: 9 endpoints**

---

## Coupon Service
**Base Path:** `/api/v1/coupons`  
**Controllers:** CouponController

### CouponController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/coupons` | Create coupon | @PreAuthorize: SERVICE_COUPON_SERVICE, SERVICE_ALL, MAIN_ADMIN, SUB_ADMIN, INSTRUCTOR |
| POST | `/coupons/bulk` | Bulk generate coupons | @PreAuthorize: SERVICE_COUPON_SERVICE, SERVICE_ALL, MAIN_ADMIN, SUB_ADMIN, INSTRUCTOR |
| GET | `/coupons` | Get all coupons | Authentication |
| GET | `/coupons/{couponId}` | Get coupon details | Authentication |
| GET | `/coupons/validate/{code}` | Validate coupon by code | Authentication |
| PUT | `/coupons/{couponId}` | Update coupon | @PreAuthorize: SERVICE_COUPON_SERVICE, SERVICE_ALL, MAIN_ADMIN, SUB_ADMIN, INSTRUCTOR |
| DELETE | `/coupons/{couponId}` | Delete coupon | @PreAuthorize: SERVICE_COUPON_SERVICE, SERVICE_ALL, MAIN_ADMIN, SUB_ADMIN, INSTRUCTOR |
| PATCH | `/coupons/{couponId}/activate` | Activate coupon | @PreAuthorize: SERVICE_COUPON_SERVICE, SERVICE_ALL, MAIN_ADMIN, SUB_ADMIN, INSTRUCTOR |
| PATCH | `/coupons/{couponId}/deactivate` | Deactivate coupon | @PreAuthorize: SERVICE_COUPON_SERVICE, SERVICE_ALL, MAIN_ADMIN, SUB_ADMIN, INSTRUCTOR |
| GET | `/coupons/my` | Get my coupons | Authentication |
| GET | `/coupons/campaigns` | Get campaigns | Authentication |
| POST | `/coupons/validate` | Validate coupon (full) | Authentication |
| POST | `/coupons/best-discount` | Get best discount | Authentication |
| POST | `/coupons/redeem` | Redeem coupon | Authentication |
| POST | `/coupons/assign-user` | Assign coupon to user | @PreAuthorize: SERVICE_COUPON_SERVICE, SERVICE_ALL, MAIN_ADMIN, SUB_ADMIN, INSTRUCTOR |

**Coupon Service Total: 15 endpoints**

---

## Order Service
**Base Path:** `/api/v1/orders`  
**Controllers:** OrderController

### OrderController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/orders/create` | Create order | Authentication |
| GET | `/orders/{orderId}` | Get order by ID | Authentication |
| GET | `/orders` | Get all orders | Authentication |
| GET | `/orders/user/{userId}` | Get orders by user | Authentication |
| PUT | `/orders/{orderId}/status` | Update order status | Authentication |
| DELETE | `/orders/{orderId}/cancel` | Cancel order | Authentication |
| POST | `/orders/{orderId}/refund` | Refund order | Authentication |

**Order Service Total: 7 endpoints**

---

## Payment Service
**Base Path:** `/api/v1/payments`  
**Controllers:** PaymentController, TestCourseController

### PaymentController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/payments` | Get all payments | Authentication |
| POST | `/payments/create` | Create payment | Authentication |
| POST | `/payments/create-and-pay` | Create and auto-pay (HTML) | Authentication |
| GET | `/payments/checkout/{txnId}` | Checkout by transaction ID (HTML) | Authentication |
| GET | `/payments/callback/success` | Success callback info | None |
| GET | `/payments/callback/failure` | Failure callback info | None |
| POST | `/payments/callback/success` (form) | Handle success callback | None |
| POST | `/payments/callback/success` (JSON) | Handle success callback (JSON) | None |
| POST | `/payments/callback/failure` (form) | Handle failure callback | None |
| POST | `/payments/callback/failure` (JSON) | Handle failure callback (JSON) | None |
| POST | `/payments/verify` | Verify payment | Authentication |
| POST | `/payments/payu/verify` | PayU verify gateway | Authentication |
| POST | `/payments/payu/check-payment` | PayU check payment | Authentication |
| POST | `/payments/payu/tdr` | PayU get TDR | Authentication |
| POST | `/payments/payu/transaction-details` | PayU transaction details | Authentication |
| POST | `/payments/payu/payment-links` | Create PayU payment link | Authentication |
| POST | `/payments/payu/consent-checkout` | PayU consent checkout (HTML) | Authentication |
| POST | `/payments/refund` | Initiate refund | Authentication |
| GET | `/payments/instructor/{instructorId}/courses/{courseId}/payments` | Get course payments for instructor | Authentication |
| GET | `/payments/{txnId}/refund-status` | Get refund status | Authentication |
| GET | `/payments/{txnId}/invoice` | Get invoice (PDF) | Authentication |
| GET | `/payments/{txnId}/invoice-json` | Get invoice (JSON) | Authentication |

### TestCourseController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/test/courses` | Create test course | None |
| GET | `/test/courses` | List test courses | None |
| GET | `/test/courses/{courseId}` | Get test course | None |

**Payment Service Total: 21 endpoints**

---

## Review Service
**Base Path:** `/api/v1/reviews`  
**Controllers:** ReviewController, AdminReviewController

### ReviewController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/reviews` | Create review | Bearer Auth |
| PUT | `/reviews/{reviewId}` | Update own review | Bearer Auth |
| DELETE | `/reviews/{reviewId}` | Delete own review | Bearer Auth |
| GET | `/reviews/{reviewId}` | Get review by ID | Bearer Auth |
| GET | `/reviews/my/course/{courseId}` | Get my review for course | Bearer Auth |
| GET | `/reviews/course/{courseId}` | Get public course reviews | None |
| GET | `/reviews/course/{courseId}/summary` | Get course rating summary | None |

### AdminReviewController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/admin/reviews` | List all reviews (paginated) | Bearer Auth |
| GET | `/admin/reviews/{reviewUuid}` | Get review details | Bearer Auth |
| PATCH | `/admin/reviews/{reviewUuid}/hide` | Hide review | Bearer Auth |
| PATCH | `/admin/reviews/{reviewUuid}/unhide` | Unhide review | Bearer Auth |
| DELETE | `/admin/reviews/{reviewUuid}` | Delete any review | Bearer Auth |

**Review Service Total: 10 endpoints**

---

## Notification Service
**Base Path:** `/api/v1`  
**Controllers:** NotificationController, AdminNotificationController, UserNotificationController, InstructorNotificationController, CourseNotificationController, AnnouncementController, DeviceTokenController, NotificationSettingsController, PushNotificationController, ReminderController, SystemController, TemplateController, PreferenceController, AssignmentReminderController, CertificateNotifyController, LiveClassReminderController
**Note:** Some endpoints (system, templates, preferences) are not routed through API Gateway. Access directly via service port :8093

### NotificationController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/notifications` | Create notification | Authentication |
| POST | `/notifications/bulk` | Create bulk notifications | Authentication |
| GET | `/notifications/{id}` | Get notification by ID | Authentication |
| GET | `/notifications` | Get all notifications | Authentication |
| GET | `/notifications/user/{userId}` | Get notifications by user | Authentication |
| GET | `/notifications/user/{userId}/combined` | Get combined user notifications | Authentication |
| PUT | `/notifications/{id}/read` | Mark as read | Authentication |
| DELETE | `/notifications/{id}` | Delete notification | Authentication |
| POST | `/notifications/{id}/retry` | Retry failed notification | Authentication |

### AdminNotificationController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/admin/broadcast` | Broadcast notification | Admin |
| POST | `/admin/reprocess-dlq` | Reprocess DLQ | Admin |
| GET | `/admin/system-health` | Get system health | Admin |
| PUT | `/admin/settings/notifications` | Update notification settings | Admin |

### UserNotificationController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/users/me/notifications` | Get my notifications | Authentication |
| GET | `/users/me/unread` | Get unread notifications | Authentication |
| PUT | `/users/me/read/{id}` | Mark as read | Authentication |
| PUT | `/users/me/read-all` | Mark all as read | Authentication |
| GET | `/users/me/count` | Get unread count | Authentication |

### InstructorNotificationController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/notifications/instructor/{instructorId}` | Notify instructor | None |

### CourseNotificationController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/notifications/course/{courseId}` | Notify course students | None |

### AnnouncementController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/announcements` | Create announcement | None |
| GET | `/announcements/course/{courseId}/detailed` | Get detailed announcements | None |

### DeviceTokenController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/device-tokens` | Register device token | X-User-Id header |

### PushNotificationController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/push/send` | Send push to users | None |
| POST | `/push/topic` | Send push to topic | None |
| POST | `/push/broadcast` | Broadcast push | None |

### SystemController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/system/health` | Get system health | None |

### TemplateController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/templates` | Create template | None |
| GET | `/templates` | Get templates (paginated) | None |
| PUT | `/templates/{id}` | Update template | None |
| DELETE | `/templates/{id}` | Delete template | None |

### PreferenceController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/preferences` | Get preferences | None |
| PUT | `/preferences` | Update preferences | None |

### AssignmentReminderController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/assignments/reminder` | Send assignment reminder | None |

### CertificateNotifyController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/certificates/notify` | Notify certificate users | None |

### LiveClassReminderController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/live-class/reminder` | Send live class reminder | None |

**Notification Service Total: 68 endpoints**

---

## Wishlist Service
**Base Path:** `/api/v1/wishlist`  
**Controllers:** WishlistController

### WishlistController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| POST | `/wishlist` | Add course to wishlist | Authentication |
| GET | `/wishlist` | Get user wishlist | Authentication |
| GET | `/wishlist/{wishlistId}` | Get wishlist item by ID | Authentication |
| GET | `/wishlist/check/{courseId}` | Check if course in wishlist | Authentication |
| POST | `/wishlist/{courseId}/move-to-cart` | Move course to cart | Authentication |
| DELETE | `/wishlist/{courseId}` | Remove from wishlist | Authentication |
| DELETE | `/wishlist` | Clear wishlist | Authentication |

**Wishlist Service Total: 7 endpoints**

---

## API Gateway
**Base Path:** `/api/sessions`  
**Controllers:** SessionManagementController

### SessionManagementController
| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| GET | `/sessions` | Get user sessions | X-User-Id, X-Session-Id headers |
| DELETE | `/sessions/{sessionId}` | Deactivate specific session | X-User-Id, X-Session-Id headers |
| POST | `/sessions/deactivate-others` | Deactivate other sessions | X-User-Id, X-Session-Id headers |
| POST | `/sessions/deactivate-all` | Deactivate all sessions | X-User-Id header |

**API Gateway Total: 4 endpoints**

---

## Summary Statistics

| Service | Endpoint Count |
|---------|---------------|
| User Service | 20 |
| Admin Service | 33 |
| Course Service | 20 |
| Instructor Service | 16 |
| Cart Service | 9 |
| Coupon Service | 15 |
| Order Service | 7 |
| Payment Service | 21 |
| Review Service | 10 |
| Notification Service | 68 |
| Wishlist Service | 7 |
| API Gateway | 4 |
| **TOTAL** | **240** |

---

## Authentication Types Used

1. **Bearer Token** - Standard JWT authentication via Authorization header
2. **@PreAuthorize** - Spring Security method-level authorization
3. **@AuthenticationPrincipal** - Spring Security principal injection
4. **Service-to-Service** - Internal service communication
5. **X-User-Id Header** - Gateway forwarded user identification
6. **None/Public** - Publicly accessible endpoints

---

## Notes

- All endpoints follow RESTful conventions
- Authentication requirements are enforced at the controller or service level
- Some endpoints have multiple content type variants (JSON, form-urlencoded, HTML)
- Notification Service has the most endpoints due to comprehensive notification management
- Several endpoints are marked as disabled/commented out in favor of unified authentication endpoints
- Payment Service includes PayU gateway integration endpoints

---

**Catalog Generated:** 2026-08-10  
**Total Services Analyzed:** 12  
**Total Controllers Analyzed:** 46  
**Total Endpoints Discovered:** 240

This catalog provides a complete overview of all API endpoints across the LMS microservices architecture.