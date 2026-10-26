# Complete API Reference

## Table of Contents

1. [User Service APIs](#user-service-apis)
2. [Course Service APIs](#course-service-apis)
3. [Cart Service APIs](#cart-service-apis)
4. [Order Service APIs](#order-service-apis)
5. [Payment Service APIs](#payment-service-apis)
6. [Review Service APIs](#review-service-apis)
7. [Wishlist Service APIs](#wishlist-service-apis)
8. [Coupon Service APIs](#coupon-service-apis)
9. [Notification Service APIs](#notification-service-apis)
10. [Admin Service APIs](#admin-service-apis)

---

## User Service APIs

### Authentication Endpoints

#### POST /api/v1/auth/register

Register a new user account and send OTP to email.

**Endpoint Identity**
- Name: User Registration
- Service: User Service (Port 8091)
- Controller: RegistrationController
- HTTP Method: POST
- Gateway Path: `/api/v1/auth/register`
- Internal Path: `/api/v1/auth/register`
- Authentication: Public (no token required)

**Request**
- Content-Type: `application/json` or `multipart/form-data`
- Body: RegisterRequest

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

**Validation Rules**
- email: Required, valid email format, unique
- password: Required, min 8 characters
- confirmPassword: Required, must match password
- firstName: Required
- lastName: Required
- mobileNumber: Optional, valid phone format
- countryCode: Optional
- dob: Optional, valid date format
- profilePhoto: Optional, valid URL
- skills: Optional, array of strings

**Response**
- Status Codes: 201 Created, 400 Bad Request, 409 Conflict
- Structure: ApiResponse<Map<String, Object>>

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

**Behavior**
- Creates user with PENDING_VERIFICATION status
- Forces role to STUDENT
- Sends OTP to email (expires in 5 minutes)
- Captures device and location info
- Max 3 OTP attempts before lockout

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/RegistrationController.java`
- Method: `registerJson()`, `registerMultipart()`, `registerFormUrlEncoded()`
- DTO: `user-service/src/main/java/com/user/register/dto/RegisterRequest.java`
- Verification: SOURCE VERIFIED

---

#### POST /api/v1/auth/verify-email

Verify email using OTP and issue JWT tokens.

**Endpoint Identity**
- Name: Email Verification
- Service: User Service (Port 8091)
- Controller: RegistrationController
- HTTP Method: POST
- Gateway Path: `/api/v1/auth/verify-email`
- Internal Path: `/api/v1/auth/verify-email`
- Authentication: Public (no token required)

**Request**
- Content-Type: `application/json`
- Body: VerifyOtpRequest

```json
{
  "email": "user@example.com",
  "otp": "123456",
  "otpSessionId": "session_abc123"
}
```

**Response**
- Status Codes: 200 OK, 400 Bad Request, 403 Forbidden, 404 Not Found, 423 Locked
- Structure: Map (LoginResponse)

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

**Cookies Set**
- `accessToken`: JWT access token (HttpOnly, Secure)
- `refreshToken`: JWT refresh token (HttpOnly, Secure)

**Behavior**
- Verifies OTP and otpSessionId
- Updates user status to ACTIVE
- Issues JWT access (15 min) and refresh (30 days) tokens
- Creates session in database
- Sets HttpOnly cookies

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/RegistrationController.java`
- Method: `verifyEmail()`
- DTO: `user-service/src/main/java/com/user/register/dto/unified/VerifyOtpRequest.java`
- Verification: SOURCE VERIFIED

---

#### POST /api/v1/auth/login

Authenticate user with email and password.

**Endpoint Identity**
- Name: User Login
- Service: User Service (Port 8091)
- Controller: UnifiedAuthenticationController
- HTTP Method: POST
- Gateway Path: `/api/v1/auth/login`
- Internal Path: `/api/v1/auth/login`
- Authentication: Public (no token required)

**Request**
- Content-Type: `application/json`
- Body: LoginRequest

```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response**
- Status Codes: 200 OK, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 423 Locked
- Structure: LoginResponse

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

**Behavior**
- Validates email and password
- Supports Student, Instructor, Admin, Super Admin
- Tracks device and location
- Creates session
- Issues JWT tokens
- Sets HttpOnly cookies

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/UnifiedAuthenticationController.java`
- Method: `login()`
- DTO: `user-service/src/main/java/com/user/register/dto/unified/LoginRequest.java`
- Verification: SOURCE VERIFIED

---

#### POST /api/v1/auth/refresh

Get new access token using refresh token.

**Endpoint Identity**
- Name: Refresh Token
- Service: User Service (Port 8091)
- Controller: UnifiedAuthenticationController
- HTTP Method: POST
- Gateway Path: `/api/v1/auth/refresh`
- Internal Path: `/api/v1/auth/refresh`
- Authentication: Requires valid refresh token

**Request**
- Headers: `Authorization: Bearer <refresh_token>`
- Body: None

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: RefreshTokenResponse

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

**Behavior**
- Validates refresh token signature and expiration
- Checks if refresh token is blacklisted
- Issues new access and refresh tokens
- Old refresh token is blacklisted

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/UnifiedAuthenticationController.java`
- Method: `refreshToken()`
- DTO: `user-service/src/main/java/com/user/register/dto/unified/RefreshTokenResponse.java`
- Verification: SOURCE VERIFIED

---

#### POST /api/v1/auth/logout

Invalidate current session and blacklist tokens.

**Endpoint Identity**
- Name: Logout
- Service: User Service (Port 8091)
- Controller: UnifiedAuthenticationController
- HTTP Method: POST
- Gateway Path: `/api/v1/auth/logout`
- Internal Path: `/api/v1/auth/logout`
- Authentication: Requires valid access token

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Body (optional):

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: Map

```json
{
  "success": true,
  "message": "Logged out successfully",
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Blacklists access token
- Blacklists refresh token (if provided)
- Removes session from database
- Clears cookies

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/UnifiedAuthenticationController.java`
- Method: `logout()`
- Verification: SOURCE VERIFIED

---

### User Profile Endpoints

#### GET /api/v1/users/me

Get current user's profile.

**Endpoint Identity**
- Name: Get User Profile
- Service: User Service (Port 8091)
- Controller: UserController
- HTTP Method: GET
- Gateway Path: `/api/v1/users/me`
- Internal Path: `/api/v1/users/me`
- Authentication: Requires JWT (any role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Query Parameters: None

**Response**
- Status Codes: 200 OK, 401 Unauthorized, 403 Forbidden
- Structure: ApiResponse<UserProfileResponse>

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

**Behavior**
- Extracts user ID from JWT token
- Fetches user profile from database
- Returns comprehensive user information

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/UserController.java`
- Method: `getProfile()`
- DTO: `user-service/src/main/java/com/user/register/dto/UserProfileResponse.java`
- Verification: SOURCE VERIFIED

---

#### PUT /api/v1/users/me

Update current user's profile.

**Endpoint Identity**
- Name: Update User Profile
- Service: User Service (Port 8091)
- Controller: UserController
- HTTP Method: PUT
- Gateway Path: `/api/v1/users/me`
- Internal Path: `/api/v1/users/me`
- Authentication: Requires JWT (any role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Content-Type: `application/json`
- Body: UpdateUserProfileRequest

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

**Response**
- Status Codes: 200 OK, 400 Bad Request, 401 Unauthorized
- Structure: ApiResponse<UserProfileResponse>

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
    ...
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Extracts user ID from JWT token
- Updates user profile in database
- Returns updated profile

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/UserController.java`
- Method: `updateProfile()`
- DTO: `user-service/src/main/java/com/user/register/dto/UpdateUserProfileRequest.java`
- Verification: SOURCE VERIFIED

---

#### PUT /api/v1/users/me/photo

Upload profile photo.

**Endpoint Identity**
- Name: Upload Profile Photo
- Service: User Service (Port 8091)
- Controller: UserController
- HTTP Method: PUT
- Gateway Path: `/api/v1/users/me/photo`
- Internal Path: `/api/v1/users/me/photo`
- Authentication: Requires JWT (any role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Content-Type: `multipart/form-data`
- Body: `file` (MultipartFile)

**Response**
- Status Codes: 200 OK, 400 Bad Request, 401 Unauthorized, 500 Internal Server Error
- Structure: ApiResponse<UserProfileResponse>

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

**Behavior**
- Uploads file to Google Drive
- Updates user profile with Google Drive URL
- Returns updated profile

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/UserController.java`
- Method: `uploadProfilePhoto()`
- Verification: SOURCE VERIFIED

---

#### DELETE /api/v1/users/me/photo

Remove profile photo.

**Endpoint Identity**
- Name: Remove Profile Photo
- Service: User Service (Port 8091)
- Controller: UserController
- HTTP Method: DELETE
- Gateway Path: `/api/v1/users/me/photo`
- Internal Path: `/api/v1/users/me/photo`
- Authentication: Requires JWT (any role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 400 Bad Request, 401 Unauthorized, 500 Internal Server Error
- Structure: ApiResponse<UserProfileResponse>

```json
{
  "success": true,
  "message": "Profile photo removed successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "profilePhoto": null,
    ...
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Sets profilePhoto to null
- Optionally deletes file from Google Drive
- Returns updated profile

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/UserController.java`
- Method: `removeProfilePhoto()`
- Verification: SOURCE VERIFIED

---

### Session Management Endpoints

#### GET /api/v1/users/me/sessions

List all active sessions for current user.

**Endpoint Identity**
- Name: List Sessions
- Service: User Service (Port 8091)
- Controller: SessionController
- HTTP Method: GET
- Gateway Path: `/api/v1/users/me/sessions`
- Internal Path: `/api/v1/users/me/sessions`
- Authentication: Requires JWT (any role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: ApiResponse<List<SessionDto>>

```json
{
  "success": true,
  "message": "Sessions fetched successfully",
  "data": [
    {
      "id": "session-xyz",
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "deviceInfo": "Chrome on Windows",
      "deviceId": "device-xyz",
      "deviceName": "Chrome on Windows",
      "deviceType": "desktop",
      "browser": "Chrome",
      "operatingSystem": "Windows 10",
      "latitude": 19.0760,
      "longitude": 72.8777,
      "city": "Mumbai",
      "country": "India",
      "ipAddress": "192.168.1.1",
      "loginTime": "2026-10-13T10:00:00"
    }
  ],
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Fetches all sessions for user
- Filters out expired sessions
- Returns session details

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/SessionController.java`
- Method: `listSessions()`
- DTO: `user-service/src/main/java/com/user/register/dto/SessionDto.java`
- Verification: SOURCE VERIFIED

---

#### DELETE /api/v1/users/me/sessions/{id}

Logout from specific device/session.

**Endpoint Identity**
- Name: Logout Device
- Service: User Service (Port 8091)
- Controller: SessionController
- HTTP Method: DELETE
- Gateway Path: `/api/v1/users/me/sessions/{id}`
- Internal Path: `/api/v1/users/me/sessions/{id}`
- Authentication: Requires JWT (any role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Path Parameters: `id` (UUID) - Session ID

**Response**
- Status Codes: 200 OK, 401 Unauthorized, 404 Not Found
- Structure: ApiResponse<Object>

```json
{
  "success": true,
  "message": "Device logged out successfully",
  "data": {
    "sessionId": "session-xyz",
    "loggedOutAt": "2026-10-13T10:30:00"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Validates session belongs to user
- Removes session from database
- Blacklists associated tokens

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/SessionController.java`
- Method: `logoutDevice()`
- Verification: SOURCE VERIFIED

---

#### DELETE /api/v1/users/me/sessions

Logout from all devices/sessions.

**Endpoint Identity**
- Name: Logout All Sessions
- Service: User Service (Port 8091)
- Controller: SessionController
- HTTP Method: DELETE
- Gateway Path: `/api/v1/users/me/sessions`
- Internal Path: `/api/v1/users/me/sessions`
- Authentication: Requires JWT (any role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 400 Bad Request, 401 Unauthorized
- Structure: ApiResponse<Object>

```json
{
  "success": true,
  "message": "Logged out from all sessions successfully",
  "data": {
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "revokedCount": 5,
    "revokedSessions": [...],
    "revokedAt": "2026-10-13T10:30:00"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Removes all sessions for user
- Blacklists all associated tokens
- Returns count of revoked sessions

**Evidence**
- Source: `user-service/src/main/java/com/user/register/controller/SessionController.java`
- Method: `logoutAllSessions()`
- Verification: SOURCE VERIFIED

---

## Course Service APIs

### Course Catalog Endpoints

#### GET /api/v1/courses

Get paginated list of courses with filtering.

**Endpoint Identity**
- Name: Get Course Catalog
- Service: Course Service (Port 8083)
- Controller: CourseController
- HTTP Method: GET
- Gateway Path: `/api/v1/courses`
- Internal Path: `/api/v1/courses`
- Authentication: Public (no token required)

**Request**
- Query Parameters:
  - `page` (int, default: 0) - Page number (0-indexed)
  - `size` (int, default: 10) - Page size (max: 100)
  - `category` (String, optional) - Filter by category
  - `level` (String, optional) - Filter by level (Beginner, Intermediate, Advanced)
  - `language` (String, optional) - Filter by language
  - `search` (String, optional) - Search in title and description
  - `sortBy` (String, optional) - Sort field (title, price, rating, createdAt)
  - `sortOrder` (String, optional) - Sort direction (asc, desc)

**Response**
- Status Codes: 200 OK
- Structure: ApiResponse<Map<String, Object>>

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

**Behavior**
- Applies filters and sorting
- Returns paginated results
- Includes instructor and enrollment info

**Evidence**
- Source: `course-service/src/main/java/com/lms/courseservice/controller/CourseController.java`
- Method: `getAllCourses()`
- Verification: SOURCE VERIFIED

---

#### GET /api/v1/courses/{id}

Get course details by ID.

**Endpoint Identity**
- Name: Get Course Details
- Service: Course Service (Port 8083)
- Controller: CourseController
- HTTP Method: GET
- Gateway Path: `/api/v1/courses/{id}`
- Internal Path: `/api/v1/courses/{id}`
- Authentication: Public (no token required)

**Request**
- Path Parameters: `id` (Long) - Course ID

**Response**
- Status Codes: 200 OK, 404 Not Found
- Structure: ApiResponse<CourseByIdResponse>

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

**Behavior**
- Fetches course by ID
- Includes comprehensive course information
- Returns 404 if course not found

**Evidence**
- Source: `course-service/src/main/java/com/lms/courseservice/controller/CourseController.java`
- Method: `getCourseById()`
- DTO: `course-service/src/main/java/com/lms/courseservice/dto/CourseByIdResponse.java`
- Verification: SOURCE VERIFIED

---

### Enrollment Endpoints

#### POST /api/v1/enrollments

Enroll user in a course.

**Endpoint Identity**
- Name: Enroll in Course
- Service: Course Service (Port 8083)
- Controller: EnrollmentController
- HTTP Method: POST
- Gateway Path: `/api/v1/enrollments`
- Internal Path: `/api/v1/enrollments`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Content-Type: `application/json`
- Body: EnrollmentRequest

```json
{
  "courseId": 1
}
```

**Response**
- Status Codes: 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict
- Structure: ApiResponse<EnrollmentResponse>

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

**Behavior**
- Validates user is STUDENT
- Checks if course exists
- Checks if already enrolled
- Creates enrollment record
- Returns enrollment details

**Evidence**
- Source: `course-service/src/main/java/com/lms/courseservice/controller/EnrollmentController.java`
- Method: `enrollInCourse()`
- Verification: SOURCE VERIFIED

---

#### GET /api/v1/enrollments/my-courses

Get all enrolled courses for current user.

**Endpoint Identity**
- Name: Get My Courses
- Service: Course Service (Port 8083)
- Controller: EnrollmentController
- HTTP Method: GET
- Gateway Path: `/api/v1/enrollments/my-courses`
- Internal Path: `/api/v1/enrollments/my-courses`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: ApiResponse<List<CourseResponse>>

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

**Behavior**
- Extracts user ID from JWT
- Fetches all enrollments for user
- Includes course and progress info

**Evidence**
- Source: `course-service/src/main/java/com/lms/courseservice/controller/EnrollmentController.java`
- Method: `getMyCourses()`
- Verification: SOURCE VERIFIED

---

#### GET /api/v1/enrollments/course/{courseId}/status

Check if user is enrolled in a course.

**Endpoint Identity**
- Name: Check Enrollment Status
- Service: Course Service (Port 8083)
- Controller: EnrollmentController
- HTTP Method: GET
- Gateway Path: `/api/v1/enrollments/course/{courseId}/status`
- Internal Path: `/api/v1/enrollments/course/{courseId}/status`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Path Parameters: `courseId` (Long) - Course ID

**Response**
- Status Codes: 200 OK, 401 Unauthorized, 404 Not Found
- Structure: ApiResponse<Boolean>

```json
{
  "success": true,
  "message": "Enrollment status fetched",
  "data": true,
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Checks if user has active enrollment
- Returns boolean

**Evidence**
- Source: `course-service/src/main/java/com/lms/courseservice/controller/EnrollmentController.java`
- Method: `checkEnrollmentStatus()`
- Verification: SOURCE VERIFIED

---

### Student Dashboard Endpoints

#### GET /api/v1/dashboard

Get student dashboard overview.

**Endpoint Identity**
- Name: Get Student Dashboard
- Service: Course Service (Port 8083)
- Controller: DashboardController
- HTTP Method: GET
- Gateway Path: `/api/v1/dashboard`
- Internal Path: `/api/v1/dashboard`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: ApiResponse<StudentDashboardResponse>

```json
{
  "success": true,
  "message": "Dashboard fetched successfully",
  "data": {
    "overview": {
      "enrolledCourses": 5,
      "completedCourses": 2,
      "inProgressCourses": 3,
      "totalProgress": 65
    },
    "recentCourses": [
      {
        "courseId": 1,
        "title": "Java Spring Boot Masterclass",
        "thumbnail": "https://example.com/thumb.jpg",
        "progress": 45,
        "lastAccessedAt": "2026-10-13T10:00:00"
      }
    ],
    "recentActivity": [
      {
        "type": "LECTURE_COMPLETED",
        "courseTitle": "Java Spring Boot Masterclass",
        "timestamp": "2026-10-13T09:00:00"
      }
    ],
    "upcomingLiveClasses": [
      {
        "liveClassId": 1,
        "courseTitle": "Java Spring Boot Masterclass",
        "title": "Q&A Session",
        "scheduledAt": "2026-10-14T10:00:00"
      }
    ],
    "pendingAssignments": [
      {
        "assignmentId": 1,
        "courseTitle": "Java Spring Boot Masterclass",
        "title": "Build REST API",
        "dueDate": "2026-10-20T23:59:59"
      }
    ],
    "certificates": [
      {
        "certificateId": "cert-xyz",
        "courseTitle": "React Basics",
        "issuedAt": "2026-09-01T00:00:00"
      }
    ]
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Aggregates student data
- Returns comprehensive dashboard overview
- Includes courses, progress, activities, live classes, assignments, certificates

**Evidence**
- Source: `course-service/src/main/java/com/lms/courseservice/controller/DashboardController.java`
- Method: `getStudentDashboard()`
- DTO: `course-service/src/main/java/com/lms/courseservice/dto/StudentDashboardResponse.java`
- Verification: SOURCE VERIFIED

---

## Cart Service APIs

#### GET /api/v1/cart

Get user's shopping cart.

**Endpoint Identity**
- Name: Get Cart
- Service: Cart Service (Port 8081)
- Controller: CartController
- HTTP Method: GET
- Gateway Path: `/api/v1/cart`
- Internal Path: `/api/v1/cart`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: ApiResponse<CartResponse>

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

**Behavior**
- Fetches cart for user
- Calculates totals
- Returns cart items

**Evidence**
- Source: `cart-service/src/main/java/com/lms/cart_service/controller/CartController.java`
- Method: `getCart()`
- Verification: SOURCE VERIFIED

---

#### POST /api/v1/cart

Add course to cart.

**Endpoint Identity**
- Name: Add to Cart
- Service: Cart Service (Port 8081)
- Controller: CartController
- HTTP Method: POST
- Gateway Path: `/api/v1/cart`
- Internal Path: `/api/v1/cart`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Content-Type: `application/json`
- Body: AddToCartRequest

```json
{
  "courseId": 1
}
```

**Response**
- Status Codes: 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict
- Structure: ApiResponse<CartResponse>

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

**Behavior**
- Validates course exists
- Checks if already in cart
- Adds item to cart
- Returns updated cart

**Evidence**
- Source: `cart-service/src/main/java/com/lms/cart_service/controller/CartController.java`
- Method: `addToCart()`
- Verification: SOURCE VERIFIED

---

#### DELETE /api/v1/cart/{itemId}

Remove item from cart.

**Endpoint Identity**
- Name: Remove from Cart
- Service: Cart Service (Port 8081)
- Controller: CartController
- HTTP Method: DELETE
- Gateway Path: `/api/v1/cart/{itemId}`
- Internal Path: `/api/v1/cart/{itemId}`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Path Parameters: `itemId` (String) - Cart item ID

**Response**
- Status Codes: 200 OK, 401 Unauthorized, 403 Forbidden, 404 Not Found
- Structure: ApiResponse<CartResponse>

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

**Behavior**
- Validates item belongs to user's cart
- Removes item from cart
- Returns updated cart

**Evidence**
- Source: `cart-service/src/main/java/com/lms/cart_service/controller/CartController.java`
- Method: `removeFromCart()`
- Verification: SOURCE VERIFIED

---

#### DELETE /api/v1/cart

Clear entire cart.

**Endpoint Identity**
- Name: Clear Cart
- Service: Cart Service (Port 8081)
- Controller: CartController
- HTTP Method: DELETE
- Gateway Path: `/api/v1/cart`
- Internal Path: `/api/v1/cart`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: ApiResponse<Void>

```json
{
  "success": true,
  "message": "Cart cleared",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Removes all items from user's cart

**Evidence**
- Source: `cart-service/src/main/java/com/lms/cart_service/controller/CartController.java`
- Method: `clearCart()`
- Verification: SOURCE VERIFIED

---

## Order Service APIs

#### POST /api/v1/orders

Create order from cart.

**Endpoint Identity**
- Name: Create Order
- Service: Order Service (Port 8084)
- Controller: OrderController
- HTTP Method: POST
- Gateway Path: `/api/v1/orders`
- Internal Path: `/api/v1/orders`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Content-Type: `application/json`
- Body: CreateOrderRequest

```json
{
  "couponCode": "SAVE20"
}
```

**Response**
- Status Codes: 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict
- Structure: ApiResponse<OrderResponse>

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

**Behavior**
- Fetches user's cart
- Applies coupon (if provided)
- Creates order
- Clears cart
- Returns order details

**Evidence**
- Source: `order-service/src/main/java/com/lms/orderservice/controller/OrderController.java`
- Method: `createOrder()`
- Verification: SOURCE VERIFIED

---

#### GET /api/v1/orders

Get user's orders.

**Endpoint Identity**
- Name: Get Orders
- Service: Order Service (Port 8084)
- Controller: OrderController
- HTTP Method: GET
- Gateway Path: `/api/v1/orders`
- Internal Path: `/api/v1/orders`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: ApiResponse<List<OrderResponse>>

```json
{
  "success": true,
  "message": "Orders fetched successfully",
  "data": [
    {
      "orderId": "order-xyz",
      "items": [...],
      "total": 23.99,
      "status": "COMPLETED",
      "createdAt": "2026-10-13T10:30:00"
    }
  ],
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Fetches all orders for user
- Returns order history

**Evidence**
- Source: `order-service/src/main/java/com/lms/orderservice/controller/OrderController.java`
- Method: `getOrders()`
- Verification: SOURCE VERIFIED

---

## Payment Service APIs

#### POST /api/v1/payments/initiate

Initiate payment for an order.

**Endpoint Identity**
- Name: Initiate Payment
- Service: Payment Service (Port 8085)
- Controller: PaymentController
- HTTP Method: POST
- Gateway Path: `/api/v1/payments/initiate`
- Internal Path: `/api/v1/payments/initiate`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Content-Type: `application/json`
- Body: PaymentInitRequest

```json
{
  "orderId": "order-xyz",
  "paymentMethod": "PAYU"
}
```

**Response**
- Status Codes: 200 OK, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found
- Structure: ApiResponse<PaymentInitResponse>

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

**Behavior**
- Validates order exists and belongs to user
- Creates payment record
- Generates PayU payment link
- Returns payment URL

**Evidence**
- Source: `payment-service/src/main/java/com/lms/paymentservice/controller/PaymentController.java`
- Method: `initiatePayment()`
- Verification: SOURCE VERIFIED

---

#### POST /api/v1/payments/verify

Verify payment callback from PayU.

**Endpoint Identity**
- Name: Verify Payment
- Service: Payment Service (Port 8085)
- Controller: PaymentController
- HTTP Method: POST
- Gateway Path: `/api/v1/payments/verify`
- Internal Path: `/api/v1/payments/verify`
- Authentication: Public (PayU callback)

**Request**
- Content-Type: `application/json`
- Body: PaymentVerifyRequest (PayU callback payload)

```json
{
  "txnid": "order-xyz",
  "status": "success",
  "amount": "23.99",
  "hash": "abc123..."
}
```

**Response**
- Status Codes: 200 OK, 400 Bad Request, 404 Not Found
- Structure: ApiResponse<PaymentVerifyResponse>

```json
{
  "success": true,
  "message": "Payment verified",
  "data": {
    "paymentId": "payment-xyz",
    "orderId": "order-xyz",
    "status": "SUCCESS",
    "verifiedAt": "2026-10-13T10:35:00"
  },
  "timestamp": "2026-10-13T10:35:00"
}
```

**Behavior**
- Verifies PayU hash
- Updates payment status
- Enrolls user in courses (if payment successful)
- Returns verification result

**Evidence**
- Source: `payment-service/src/main/java/com/lms/paymentservice/controller/PaymentController.java`
- Method: `verifyPayment()`
- Verification: SOURCE VERIFIED

---

## Review Service APIs

#### POST /api/v1/reviews

Submit course review.

**Endpoint Identity**
- Name: Submit Review
- Service: Review Service (Port 8089)
- Controller: ReviewController
- HTTP Method: POST
- Gateway Path: `/api/v1/reviews`
- Internal Path: `/api/v1/reviews`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Content-Type: `application/json`
- Body: ReviewRequest

```json
{
  "courseId": 1,
  "rating": 5,
  "comment": "Excellent course! Very informative."
}
```

**Response**
- Status Codes: 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict
- Structure: ApiResponse<ReviewResponse>

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

**Behavior**
- Validates user is enrolled in course
- Checks if already reviewed
- Creates review
- Updates course rating

**Evidence**
- Source: `review-service/src/main/java/com/lms/review/controller/ReviewController.java`
- Method: `submitReview()`
- Verification: SOURCE VERIFIED

---

#### GET /api/v1/reviews/course/{courseId}

Get reviews for a course.

**Endpoint Identity**
- Name: Get Course Reviews
- Service: Review Service (Port 8089)
- Controller: ReviewController
- HTTP Method: GET
- Gateway Path: `/api/v1/reviews/course/{courseId}`
- Internal Path: `/api/v1/reviews/course/{courseId}`
- Authentication: Public (no token required)

**Request**
- Path Parameters: `courseId` (Long) - Course ID
- Query Parameters:
  - `page` (int, default: 0)
  - `size` (int, default: 10)

**Response**
- Status Codes: 200 OK, 404 Not Found
- Structure: ApiResponse<List<ReviewResponse>>

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

**Behavior**
- Fetches reviews for course
- Returns paginated results

**Evidence**
- Source: `review-service/src/main/java/com/lms/review/controller/ReviewController.java`
- Method: `getCourseReviews()`
- Verification: SOURCE VERIFIED

---

## Wishlist Service APIs

#### GET /api/v1/wishlist

Get user's wishlist.

**Endpoint Identity**
- Name: Get Wishlist
- Service: Wishlist Service (Port 8090)
- Controller: WishlistController
- HTTP Method: GET
- Gateway Path: `/api/v1/wishlist`
- Internal Path: `/api/v1/wishlist`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`

**Response**
- Status Codes: 200 OK, 401 Unauthorized
- Structure: ApiResponse<List<WishlistResponse>>

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

**Behavior**
- Fetches wishlist for user
- Returns course details

**Evidence**
- Source: `wishlist-service/src/main/java/com/lms/wishlist_service/controller/WishlistController.java`
- Method: `getWishlist()`
- Verification: SOURCE VERIFIED

---

#### POST /api/v1/wishlist

Add course to wishlist.

**Endpoint Identity**
- Name: Add to Wishlist
- Service: Wishlist Service (Port 8090)
- Controller: WishlistController
- HTTP Method: POST
- Gateway Path: `/api/v1/wishlist`
- Internal Path: `/api/v1/wishlist`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Content-Type: `application/json`
- Body: AddToWishlistRequest

```json
{
  "courseId": 1
}
```

**Response**
- Status Codes: 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict
- Structure: ApiResponse<WishlistResponse>

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

**Behavior**
- Validates course exists
- Checks if already in wishlist
- Adds to wishlist
- Returns wishlist item

**Evidence**
- Source: `wishlist-service/src/main/java/com/lms/wishlist_service/controller/WishlistController.java`
- Method: `addToWishlist()`
- Verification: SOURCE VERIFIED

---

#### DELETE /api/v1/wishlist/{courseId}

Remove course from wishlist.

**Endpoint Identity**
- Name: Remove from Wishlist
- Service: Wishlist Service (Port 8090)
- Controller: WishlistController
- HTTP Method: DELETE
- Gateway Path: `/api/v1/wishlist/{courseId}`
- Internal Path: `/api/v1/wishlist/{courseId}`
- Authentication: Requires JWT (STUDENT role)

**Request**
- Headers: `Authorization: Bearer <access_token>`
- Path Parameters: `courseId` (Long) - Course ID

**Response**
- Status Codes: 200 OK, 401 Unauthorized, 403 Forbidden, 404 Not Found
- Structure: ApiResponse<Void>

```json
{
  "success": true,
  "message": "Removed from wishlist",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

**Behavior**
- Validates item belongs to user's wishlist
- Removes from wishlist

**Evidence**
- Source: `wishlist-service/src/main/java/com/lms/wishlist_service/controller/WishlistController.java`
- Method: `removeFromWishlist()`
- Verification: SOURCE VERIFIED

---

## Note

This reference covers the most critical and commonly used endpoints. For a complete inventory of all 127+ endpoints, see **API_ENDPOINT_INVENTORY.csv**.

Additional endpoints for:
- Quiz management and attempts
- Assignment submission and grading
- Live class management and attendance
- Certificate generation and verification
- Banner management
- Notification preferences
- Admin operations

are documented in the endpoint inventory CSV but not fully detailed in this reference due to scope.
