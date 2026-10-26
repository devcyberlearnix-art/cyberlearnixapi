# Authentication and Authorization

## Overview

The Asivo LMS uses JWT (JSON Web Token) based authentication with role-based access control (RBAC). This document details the complete authentication flow, token structure, and authorization rules.

## Authentication Endpoints

### 1. User Registration

**Endpoint**: `POST /api/v1/auth/register`

**Purpose**: Register a new user account and send OTP to email for verification.

**Request Body** (JSON):
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

**Alternative: Multipart Form Data** (for file upload):
- Form field: `data` (JSON string of above object)
- Form field: `profilePhoto` (MultipartFile)

**Response** (201 Created):
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
    "expiresInSeconds": 300,
    "deviceId": "device_xyz",
    "deviceName": "Chrome on Windows",
    "deviceType": "desktop",
    "browser": "Chrome",
    "os": "Windows 10",
    "ipAddress": "192.168.1.1",
    "latitude": 19.0760,
    "longitude": 72.8777,
    "city": "Mumbai",
    "country": "India"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Public (no token required)

**Business Rules**:
- Role is forced to STUDENT on registration
- Instructor status is false by default
- OTP is sent to email and expires in 5 minutes
- Max 3 OTP attempts before lockout
- Device and location info is captured

### 2. Email Verification (OTP)

**Endpoint**: `POST /api/v1/auth/verify-email`

**Purpose**: Verify email using OTP and issue JWT tokens.

**Request Body**:
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "otpSessionId": "session_abc123"
}
```

**Response** (200 OK):
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

**Cookies Set**:
- `accessToken`: JWT access token (HttpOnly, Secure)
- `refreshToken`: JWT refresh token (HttpOnly, Secure)

**Authentication**: Public (no token required)

**Error Responses**:
- 400 Bad Request: Invalid OTP
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
- 423 Locked: Account locked (too many failed attempts)
- 403 Forbidden: Account suspended
- 404 Not Found: User not found
- 400 Bad Request: OTP expired

### 3. Login (Password)

**Endpoint**: `POST /api/v1/auth/login`

**Purpose**: Authenticate user with email and password.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response** (200 OK):
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

**Authentication**: Public (no token required)

**Business Rules**:
- Supports Student, Instructor, Admin, Super Admin login
- Device and location tracking
- Session creation in database
- Cookies set with tokens

### 4. Login (OTP-Based)

**Endpoint**: `POST /api/v1/auth/login/otp/request`

**Purpose**: Request OTP for passwordless login.

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "OTP sent to email",
  "data": {
    "otpSessionId": "session_abc123",
    "expiresInSeconds": 300
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Public (no token required)

### 5. Verify Login OTP

**Endpoint**: `POST /api/v1/auth/login/otp/verify`

**Purpose**: Verify OTP and issue tokens (same response as password login).

**Request Body**:
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "otpSessionId": "session_abc123"
}
```

**Response**: Same as password login (200 OK)

**Authentication**: Public (no token required)

### 6. Refresh Token

**Endpoint**: `POST /api/v1/auth/refresh`

**Purpose**: Get new access token using refresh token.

**Request Headers**:
```
Authorization: Bearer <refresh_token>
```

**Response** (200 OK):
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

**Authentication**: Requires valid refresh token

**Error Responses**:
- 401 Unauthorized: Invalid or expired refresh token
- 401 Unauthorized: Refresh token blacklisted

### 7. Logout

**Endpoint**: `POST /api/v1/auth/logout`

**Purpose**: Invalidate current session and blacklist tokens.

**Request Headers**:
```
Authorization: Bearer <access_token>
```

**Request Body** (optional):
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Logged out successfully",
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Requires valid access token

**Business Rules**:
- Access token is blacklisted
- Refresh token is blacklisted (if provided)
- Session is removed from database
- Cookies are cleared

### 8. Forgot Password

**Endpoint**: `POST /api/v1/auth/password/forgot`

**Purpose**: Request password reset OTP.

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Password reset OTP sent to email",
  "data": {
    "otpSessionId": "session_abc123",
    "expiresInSeconds": 300
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Public (no token required)

### 9. Verify Password Reset OTP

**Endpoint**: `POST /api/v1/auth/password/verify-otp`

**Purpose**: Verify OTP for password reset.

**Request Body**:
```json
{
  "email": "user@example.com",
  "otp": "123456",
  "otpSessionId": "session_abc123"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "OTP verified. Proceed to reset password.",
  "data": {
    "verified": true,
    "otpSessionId": "session_abc123"
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Public (no token required)

### 10. Reset Password

**Endpoint**: `POST /api/v1/auth/password/reset`

**Purpose**: Reset password with new password.

**Request Body**:
```json
{
  "email": "user@example.com",
  "otpSessionId": "session_abc123",
  "newPassword": "NewSecurePass123!",
  "confirmPassword": "NewSecurePass123!"
}
```

**Request Headers** (optional - if using authenticated reset):
```
Authorization: Bearer <access_token>
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Password reset successfully",
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Public (or authenticated if using token)

### 11. Change Password (Authenticated)

**Endpoint**: `POST /api/v1/auth/change-password`

**Purpose**: Change password while logged in.

**Request Headers**:
```
Authorization: Bearer <access_token>
```

**Request Body**:
```json
{
  "currentPassword": "OldSecurePass123!",
  "newPassword": "NewSecurePass123!",
  "confirmPassword": "NewSecurePass123!"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Password changed successfully",
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Requires valid access token

### 12. Switch Role

**Endpoint**: `POST /api/v1/auth/switch-role`

**Purpose**: Switch between user roles (e.g., Student ↔ Instructor).

**Request Headers**:
```
Authorization: Bearer <access_token>
```

**Request Body**:
```json
{
  "targetRole": "INSTRUCTOR"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Role switched successfully",
  "data": {
    "previousRole": "STUDENT",
    "currentRole": "INSTRUCTOR",
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Requires valid access token

**Business Rules**:
- User must have the target role approved
- New tokens are issued with updated role claim
- Previous tokens are blacklisted

### 13. Resend OTP (Common)

**Endpoint**: `POST /api/v1/auth/otp/resend`

**Purpose**: Resend OTP for various flows (registration, login, password reset).

**Request Body**:
```json
{
  "otpSessionId": "session_abc123",
  "flow": "REGISTRATION"
}
```

**Flow Types**: `REGISTRATION`, `LOGIN`, `PASSWORD_RESET`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "OTP resent successfully",
  "data": {
    "otpSessionId": "session_abc123",
    "expiresInSeconds": 300
  },
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Public (no token required)

### 14. Social Login

**Endpoint**: `POST /api/v1/auth/social-login`

**Purpose**: Login via OAuth2 provider (Google, GitHub, LinkedIn).

**Request Body**:
```json
{
  "provider": "google",
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6Ij...",
  "accessToken": "ya29.a0AfH6SMB..."
}
```

**Alternative**: `POST /api/v1/auth/social-login/{provider}`

**Response**: Same as password login (200 OK)

**Authentication**: Public (no token required)

**Supported Providers**:
- Google
- GitHub
- LinkedIn

### 15. Get OAuth Providers

**Endpoint**: `GET /api/v1/auth/oauth/providers`

**Purpose**: Get available OAuth providers and redirect info.

**Response** (200 OK):
```json
{
  "success": true,
  "providers": [
    {
      "name": "google",
      "displayName": "Google",
      "authorizationUrl": "https://accounts.google.com/o/oauth2/v2/auth",
      "scopes": ["email", "profile"],
      "enabled": true
    },
    {
      "name": "github",
      "displayName": "GitHub",
      "authorizationUrl": "https://github.com/login/oauth/authorize",
      "scopes": ["read:user", "user:email"],
      "enabled": true
    },
    {
      "name": "linkedin",
      "displayName": "LinkedIn",
      "authorizationUrl": "https://www.linkedin.com/oauth/v2/authorization",
      "scopes": ["openid", "profile", "email"],
      "enabled": true
    }
  ],
  "timestamp": "2026-10-13T10:30:00"
}
```

**Authentication**: Public (no token required)

## JWT Token Structure

### Access Token Claims

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "role": "ROLE_STUDENT",
  "type": "access",
  "iss": "cyberlearnix",
  "aud": "cyberlearnix-clients",
  "iat": 1697220600,
  "exp": 1697221500
}
```

### Refresh Token Claims

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "role": "ROLE_STUDENT",
  "type": "refresh",
  "iss": "cyberlearnix",
  "aud": "cyberlearnix-clients",
  "iat": 1697220600,
  "exp": 1699812600
}
```

### Token Expiration

- **Access Token**: 15 minutes (900 seconds)
- **Refresh Token**: 30 days (2,592,000 seconds)

### Token Configuration

- **Algorithm**: HS256
- **Secret**: Configured via `JWT_SECRET` environment variable
- **Issuer**: `cyberlearnix`
- **Audience**: `cyberlearnix-clients`

## Role-Based Access Control

### User Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| STUDENT | Regular student | View courses, enroll, take quizzes, submit assignments |
| INSTRUCTOR | Approved instructor | Create courses, manage content, grade students |
| SUB_ADMIN | Limited admin | Manage users, view reports, moderate content |
| MAIN_ADMIN | Full admin | All permissions including user management |
| SUPER_ADMIN | Highest level | All permissions including system configuration |

### Role Precedence

For users with multiple roles (e.g., Student + Instructor):
- `effectiveRole` field indicates the currently active role
- Role can be switched via `/api/v1/auth/switch-role`
- Default role is the first approved role

### Authorization Annotations

Spring Security annotations used in controllers:

```java
@PreAuthorize("hasAnyRole('INSTRUCTOR','MAIN_ADMIN','SUB_ADMIN')")
@PreAuthorize("hasRole('MAIN_ADMIN')")
@PreAuthorize("isAuthenticated()")
```

### Gateway Authorization

Gateway validates JWT and passes headers to downstream services:
- `X-User-Id`: User UUID
- `X-User-Email`: User email
- `X-User-Role`: User role
- `X-User-Effective-Role`: Current active role

## Session Management

### Session Endpoints

**List Sessions**: `GET /api/v1/users/me/sessions`
**Logout Device**: `DELETE /api/v1/users/me/sessions/{id}`
**Logout All**: `DELETE /api/v1/users/me/sessions`

### Session Information

Each session tracks:
- Device ID and name
- Browser and OS
- IP address
- Location (city, country, lat, long)
- Login time
- Expiration time

### Session Expiration

- Sessions expire after 30 days of inactivity
- Expired sessions are filtered from list responses
- Logout removes session from database

## Token Blacklisting

Blacklisted tokens are stored in Redis:
- Key: `blacklist:{token_hash}`
- TTL: Matches token expiration
- Checked on every protected request

## Public vs Protected Endpoints

### Public Endpoints (No Authentication)

- `/api/v1/auth/register`
- `/api/v1/auth/verify-email`
- `/api/v1/auth/login`
- `/api/v1/auth/login/otp/*`
- `/api/v1/auth/password/forgot`
- `/api/v1/auth/password/verify-otp`
- `/api/v1/auth/password/reset`
- `/api/v1/auth/otp/resend`
- `/api/v1/auth/social-login`
- `/api/v1/auth/oauth/providers`
- `/api/v1/courses` (public catalog)
- `/api/v1/banners` (public banners)

### Protected Endpoints (Require Authentication)

- `/api/v1/users/**` (except public upload)
- `/api/v1/cart/**`
- `/api/v1/orders/**`
- `/api/v1/payments/**`
- `/api/v1/wishlist/**`
- `/api/v1/dashboard/**`
- `/api/v1/enrollments/**`
- `/api/v1/admin/**`

## CORS Configuration

### Allowed Origins

- `http://localhost:3000`
- `http://localhost:3001`
- `https://*.ngrok-free.app`
- `https://*.ngrok-free.dev`
- `https://*.ngrok.io`

### Allowed Methods

GET, POST, PUT, DELETE, OPTIONS, PATCH

### Allowed Headers

Authorization, Content-Type, Accept, Origin, X-Requested-With, ngrok-skip-browser-warning

### Credentials

`allowCredentials: true` (cookies supported)

## Error Responses

### 401 Unauthorized

```json
{
  "success": false,
  "message": "Access token expired. Please refresh your token.",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

### 403 Forbidden

```json
{
  "success": false,
  "message": "Access denied. Insufficient permissions.",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

### 423 Locked

```json
{
  "success": false,
  "message": "Account locked due to too many failed attempts.",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

## Security Best Practices

1. **Always use HTTPS** in production
2. **Store tokens securely** (HttpOnly cookies or secure storage)
3. **Refresh tokens before expiration** (automatic refresh recommended)
4. **Logout properly** to blacklist tokens
5. **Validate token expiration** on every request
6. **Use role-based UI** based on user's effective role
7. **Handle 401 errors** by refreshing token or redirecting to login
8. **Never store tokens in localStorage** (vulnerable to XSS)

## OAuth2 Configuration

### Google OAuth2

- Client ID: Configured via `GOOGLE_CLIENT_ID`
- Client Secret: Configured via `GOOGLE_CLIENT_SECRET`
- Scopes: email, profile
- Redirect URI: `{baseUrl}/login/oauth2/code/google`

### GitHub OAuth2

- Client ID: Configured via `GITHUB_CLIENT_ID`
- Client Secret: Configured via `GITHUB_CLIENT_SECRET`
- Scopes: read:user, user:email
- Redirect URI: `{baseUrl}/login/oauth2/code/github`

### LinkedIn OAuth2

- Client ID: Configured via `LINKEDIN_CLIENT_ID`
- Client Secret: Configured via `LINKEDIN_CLIENT_SECRET`
- Scopes: openid, profile, email
- Redirect URI: `{baseUrl}/login/oauth2/code/linkedin`

## Known Issues

See **API_CONTRACT_GAPS.md** for authentication-related issues:
- Token refresh behavior needs runtime verification
- Session management edge cases
- OAuth2 provider configuration inconsistencies
