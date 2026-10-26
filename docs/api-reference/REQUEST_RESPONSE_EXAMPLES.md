# Request and Response Examples

This document provides copyable request and response examples for the most commonly used API endpoints.

## Table of Contents

1. [Authentication Examples](#authentication-examples)
2. [User Profile Examples](#user-profile-examples)
3. [Course Catalog Examples](#course-catalog-examples)
4. [Enrollment Examples](#enrollment-examples)
5. [Cart Examples](#cart-examples)
6. [Order Examples](#order-examples)
7. [Payment Examples](#payment-examples)
8. [Review Examples](#review-examples)
9. [Wishlist Examples](#wishlist-examples)

---

## Authentication Examples

### Register User

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!",
    "confirmPassword": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "mobileNumber": "+919876543210",
    "countryCode": "+91"
  }'
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "mobileNumber": "+919876543210",
  "countryCode": "+91",
  "dob": "1990-01-15",
  "profilePhoto": "https://example.com/photo.jpg",
  "city": "Mumbai",
  "state": "Maharashtra",
  "country": "India",
  "preferredLanguage": "en",
  "organization": "Example Corp",
  "skills": ["Java", "Spring Boot"],
  "fieldOfStudy": "Computer Science",
  "highestQualification": "B.Tech"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully. OTP has been sent to email.",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "status": "PENDING_VERIFICATION",
    "role": "STUDENT",
    "countryCode": "+91",
    "effectiveRole": "STUDENT",
    "skills": ["Java", "Spring Boot"],
    "profilePhoto": "https://example.com/photo.jpg",
    "otpSessionId": "session_abc123",
    "expiresInSeconds": 300
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Error Response (400 Bad Request - Email already exists):**
```json
{
  "success": false,
  "message": "Email already registered",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Verify Email (OTP)

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "otp": "123456",
    "otpSessionId": "session_abc123"
  }'
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "otpSessionId": "session_abc123"
}
```

**Success Response (200 OK):**
```json
{
  "authentication": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjoiUk9MRV9TVFVERU5UIiwidHlwZSI6ImFjY2VzcyIsImlzcyI6ImN5YmVybGVhcm5peCIsImF1ZCI6ImN5YmVybGVhcm5peC1jbGllbnRzIiwiaWF0IjoxNjk3MjIwNjAwLCJleHAiOjE2OTcyMjE1MDB9.abc123xyz",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6InVzZXJAZXhhbXBsZS5jb20iLCJyb2xlIjoiUk9MRV9TVFVERU5UIiwidHlwZSI6InJlZnJlc2giLCJpc3MiOiJjeWJlcmxlYXJuaXgiLCJhdWQiOiJjeWJlcmxlYXJuaXgtY2xpZW50cyIsImlhdCI6MTY5NzIyMDYwMCwiZXhwIjoxNjk5ODEyNjAwfQ.def456uvw",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "STUDENT",
    "status": "ACTIVE"
  },
  "sessionInfo": {
    "sessionId": "session_xyz",
    "deviceId": "device_xyz",
    "deviceName": "Chrome on Windows",
    "ipAddress": "192.168.1.1",
    "location": "Mumbai, India"
  }
}
```

**Error Response (400 Bad Request - Invalid OTP):**
```json
{
  "success": false,
  "message": "Invalid OTP",
  "data": {
    "remainingAttempts": 2,
    "expiresInSeconds": 240
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Login

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePass123!"
  }'
```

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Success Response (200 OK):**
```json
{
  "authentication": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  },
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "STUDENT",
    "status": "ACTIVE",
    "effectiveRole": "STUDENT"
  },
  "sessionInfo": {
    "sessionId": "session_xyz",
    "deviceId": "device_xyz",
    "deviceName": "Chrome on Windows",
    "ipAddress": "192.168.1.1",
    "location": "Mumbai, India"
  }
}
```

**Error Response (401 Unauthorized - Invalid credentials):**
```json
{
  "success": false,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Refresh Token

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Authorization: Bearer <REFRESH_TOKEN>"
```

**Request Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "timestamp": "2026-10-13T10:30:00"
}
```

**Error Response (401 Unauthorized - Invalid refresh token):**
```json
{
  "success": false,
  "message": "Invalid or expired refresh token",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Logout

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<REFRESH_TOKEN>"
  }'
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Request Body (optional):**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## User Profile Examples

### Get User Profile

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User profile fetched successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "mobile": "+919876543210",
    "countryCode": "+91",
    "dob": "1990-01-15",
    "city": "Mumbai",
    "state": "Maharashtra",
    "country": "India",
    "preferredLanguage": "en",
    "organization": "Example Corp",
    "skills": ["Java", "Spring Boot"],
    "fieldOfStudy": "Computer Science",
    "highestQualification": "B.Tech",
    "profilePhoto": "https://example.com/photo.jpg",
    "coverPhoto": "https://example.com/cover.jpg",
    "role": "STUDENT",
    "status": "ACTIVE",
    "isInstructorApproved": false,
    "effectiveRole": "STUDENT",
    "createdAt": "2026-01-01T00:00:00"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Error Response (401 Unauthorized - Token expired):**
```json
{
  "success": false,
  "message": "Access token expired. Please refresh your token.",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Update User Profile

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Smith",
    "mobile": "+919876543211",
    "city": "Bangalore",
    "state": "Karnataka"
  }'
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "mobile": "+919876543211",
  "countryCode": "+91",
  "dob": "1990-01-15",
  "city": "Bangalore",
  "state": "Karnataka",
  "country": "India",
  "preferredLanguage": "en",
  "organization": "New Corp",
  "skills": ["Java", "Spring Boot", "React"],
  "fieldOfStudy": "Computer Science",
  "highestQualification": "M.Tech"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Smith",
    "mobile": "+919876543211",
    "city": "Bangalore",
    "state": "Karnataka",
    ...
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Upload Profile Photo

**cURL Example:**
```bash
curl -X PUT http://localhost:8080/api/v1/users/me/photo \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -F "file=@/path/to/photo.jpg"
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: multipart/form-data
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Profile photo updated successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "profilePhoto": "https://drive.google.com/file/d/abc123/view",
    ...
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## Course Catalog Examples

### Get Course Catalog

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/courses?page=0&size=10&category=Programming&level=Intermediate"
```

**Query Parameters:**
```
page=0
size=10
category=Programming
level=Intermediate
language=English
search=Spring
sortBy=price
sortOrder=asc
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Courses fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Java Spring Boot Masterclass",
        "description": "Complete guide to Spring Boot",
        "instructorId": "550e8400-e29b-41d4-a716-446655440000",
        "instructorName": "John Doe",
        "category": "Programming",
        "level": "Intermediate",
        "language": "English",
        "price": 49.99,
        "discountPrice": 29.99,
        "thumbnail": "https://example.com/thumb.jpg",
        "rating": 4.5,
        "reviewCount": 150,
        "enrollmentCount": 1200,
        "duration": "20 hours",
        "createdAt": "2026-01-01T00:00:00"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10,
    "first": true,
    "last": false
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Get Course Details

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/v1/courses/1
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Course fetched successfully",
  "data": {
    "id": 1,
    "title": "Java Spring Boot Masterclass",
    "description": "Complete guide to Spring Boot",
    "instructorId": "550e8400-e29b-41d4-a716-446655440000",
    "instructorName": "John Doe",
    "instructorProfilePhoto": "https://example.com/instructor.jpg",
    "category": "Programming",
    "level": "Intermediate",
    "language": "English",
    "price": 49.99,
    "discountPrice": 29.99,
    "thumbnail": "https://example.com/thumb.jpg",
    "rating": 4.5,
    "reviewCount": 150,
    "enrollmentCount": 1200,
    "duration": "20 hours",
    "requirements": ["Basic Java knowledge", "IDE installed"],
    "learningOutcomes": ["Build REST APIs", "Database integration"],
    "targetAudience": ["Java developers", "Backend developers"],
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-15T00:00:00"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## Enrollment Examples

### Enroll in Course

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/enrollments \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 1
  }'
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "courseId": 1
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Enrolled successfully",
  "data": {
    "enrollmentId": "enroll-xyz",
    "courseId": 1,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "enrolledAt": "2026-10-13T10:30:00",
    "status": "ACTIVE",
    "progress": 0
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Error Response (409 Conflict - Already enrolled):**
```json
{
  "success": false,
  "message": "Already enrolled in this course",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Get My Courses

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/v1/enrollments/my-courses \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Enrolled courses fetched successfully",
  "data": [
    {
      "courseId": 1,
      "title": "Java Spring Boot Masterclass",
      "thumbnail": "https://example.com/thumb.jpg",
      "instructorName": "John Doe",
      "progress": 45,
      "enrolledAt": "2026-01-01T00:00:00",
      "lastAccessedAt": "2026-10-13T10:00:00"
    }
  ],
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## Cart Examples

### Get Cart

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/v1/cart \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Cart fetched successfully",
  "data": {
    "cartId": "cart-xyz",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "itemId": "item-xyz",
        "courseId": 1,
        "title": "Java Spring Boot Masterclass",
        "thumbnail": "https://example.com/thumb.jpg",
        "instructorName": "John Doe",
        "price": 49.99,
        "discountPrice": 29.99,
        "addedAt": "2026-10-13T10:00:00"
      }
    ],
    "subtotal": 29.99,
    "discount": 0,
    "total": 29.99
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Add to Cart

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/cart \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 1
  }'
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "courseId": 1
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Course added to cart",
  "data": {
    "cartId": "cart-xyz",
    "items": [...],
    "subtotal": 59.98,
    "total": 59.98
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Remove from Cart

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/v1/cart/item-xyz \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Item removed from cart",
  "data": {
    "cartId": "cart-xyz",
    "items": [...],
    "subtotal": 29.99,
    "total": 29.99
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## Order Examples

### Create Order

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "couponCode": "SAVE20"
  }'
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "couponCode": "SAVE20"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "orderId": "order-xyz",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "courseId": 1,
        "title": "Java Spring Boot Masterclass",
        "price": 49.99,
        "discountPrice": 29.99
      }
    ],
    "subtotal": 29.99,
    "discount": 6.00,
    "total": 23.99,
    "status": "PENDING_PAYMENT",
    "createdAt": "2026-10-13T10:30:00"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## Payment Examples

### Initiate Payment

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "order-xyz",
    "paymentMethod": "PAYU"
  }'
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "orderId": "order-xyz",
  "paymentMethod": "PAYU"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment initiated",
  "data": {
    "paymentId": "payment-xyz",
    "orderId": "order-xyz",
    "amount": 23.99,
    "paymentUrl": "https://test.payu.in/payment/abc123",
    "expiresAt": "2026-10-13T11:30:00"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## Review Examples

### Submit Review

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 1,
    "rating": 5,
    "comment": "Excellent course! Very informative."
  }'
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "courseId": 1,
  "rating": 5,
  "comment": "Excellent course! Very informative."
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Review submitted",
  "data": {
    "reviewId": "review-xyz",
    "courseId": 1,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "userName": "John Doe",
    "rating": 5,
    "comment": "Excellent course! Very informative.",
    "createdAt": "2026-10-13T10:30:00"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Get Course Reviews

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/reviews/course/1?page=0&size=10"
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Reviews fetched",
  "data": [
    {
      "reviewId": "review-xyz",
      "userName": "John Doe",
      "rating": 5,
      "comment": "Excellent course!",
      "createdAt": "2026-10-13T10:30:00"
    }
  ],
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## Wishlist Examples

### Get Wishlist

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/v1/wishlist \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Wishlist fetched",
  "data": [
    {
      "wishlistId": "wish-xyz",
      "courseId": 1,
      "title": "Java Spring Boot Masterclass",
      "thumbnail": "https://example.com/thumb.jpg",
      "instructorName": "John Doe",
      "price": 49.99,
      "discountPrice": 29.99,
      "addedAt": "2026-10-13T10:00:00"
    }
  ],
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Add to Wishlist

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/wishlist \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": 1
  }'
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body:**
```json
{
  "courseId": 1
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Added to wishlist",
  "data": {
    "wishlistId": "wish-xyz",
    "courseId": 1,
    "addedAt": "2026-10-13T10:30:00"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

---

### Remove from Wishlist

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/v1/wishlist/1 \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

**Request Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Removed from wishlist",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

---

## Note

All examples use:
- Base URL: `http://localhost:8080` (API Gateway)
- Placeholder tokens: `<ACCESS_TOKEN>`, `<REFRESH_TOKEN>`
- Placeholder IDs: `xyz` for dynamically generated IDs

Replace placeholders with actual values from your environment.
