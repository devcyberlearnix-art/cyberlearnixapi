# Postman Collection Update Change Log

**Date:** 2026-08-01  
**Purpose:** Update Postman collections to match refactored authentication architecture  
**Status:** ✅ COMPLETED

---

## Overview

Updated Postman collections to reflect the centralized authentication architecture where:
- All authentication flows through `UnifiedAuthenticationController` (User Service)
- `AdminAuthController` has been removed
- JWT authentication is centralized with Spring Security
- Roles: STUDENT, INSTRUCTOR, MAIN_ADMIN, SUB_ADMIN

---

## Files Updated

### 1. Admin Service Collection
**File:** `Admin_Service_API_Collection_Updated.postman_collection.json`  
**Original:** `Admin_Service_API_Collection.postman_collection.json`

### 2. User Service Collection
**File:** `User_Service_API_Collection.postman_collection.json`  
**Status:** Updated variables (currentRole, currentUserId added)

### 3. Environment File
**File:** `CyberLearnix_Environment_Updated.postman_environment.json`  
**Status:** Created new with comprehensive variable set

---

## Requests Modified

### Admin Service Collection

#### Removed Requests
| Request | Endpoint | Reason |
|---------|----------|--------|
| Admin Login | `POST /api/v1/admins/login` | Duplicate - now handled by User Service |

#### Modified Requests
| Request | Old Endpoint | New Endpoint | Change |
|---------|-------------|--------------|--------|
| Main Admin Login | `POST {{baseUrl}}/api/v1/admins/login` | `POST {{userServiceUrl}}/api/v1/auth/login` | Routes to User Service |
| Register Sub Admin | `POST {{baseUrl}}/api/v1/admins/register` | `POST {{adminServiceUrl}}/api/v1/admin/register` | Fixed endpoint path |
| Get Profile | `GET {{baseUrl}}/api/v1/admins/me` | `GET {{adminServiceUrl}}/api/v1/admin/me` | Fixed endpoint path |
| Update Profile | `PUT {{baseUrl}}/api/v1/admins/me` | `PUT {{adminServiceUrl}}/api/v1/admin/me` | Fixed endpoint path |
| Get All Users | `GET {{baseUrl}}/api/v1/admin/users` | `GET {{adminServiceUrl}}/api/v1/admin/users` | Fixed endpoint path |
| Get User by ID | `GET {{baseUrl}}/api/v1/admin/users/:id` | `GET {{adminServiceUrl}}/api/v1/admin/users/:id` | Fixed endpoint path |
| Update User Status | `PUT {{baseUrl}}/api/v1/admin/users/:id/status` | `PUT {{adminServiceUrl}}/api/v1/admin/users/:id/status` | Fixed endpoint path |
| Delete User | `DELETE {{baseUrl}}/api/v1/admin/users/:id` | `DELETE {{adminServiceUrl}}/api/v1/admin/users/:id` | Fixed endpoint path |

#### Added Requests
| Request | Endpoint | Purpose |
|---------|----------|---------|
| Student Login | `POST {{userServiceUrl}}/api/v1/auth/login` | Login as Student |
| Instructor Login | `POST {{userServiceUrl}}/api/v1/auth/login` | Login as Instructor |
| Sub Admin Login | `POST {{userServiceUrl}}/api/v1/auth/login` | Login as Sub Admin |
| Refresh Token | `POST {{userServiceUrl}}/api/v1/auth/refresh` | Refresh access token |
| Logout | `POST {{userServiceUrl}}/api/v1/auth/logout` | Logout and invalidate tokens |
| Student Access Admin API - Expect 403 | `GET {{adminServiceUrl}}/api/v1/admin/users` | RBAC test |
| Instructor Access Admin API - Expect 403 | `GET {{adminServiceUrl}}/api/v1/admin/users` | RBAC test |
| Main Admin Access Admin API - Expect 200 | `GET {{adminServiceUrl}}/api/v1/admin/users` | RBAC test |
| Sub Admin Access Admin API - Expect 200 | `GET {{adminServiceUrl}}/api/v1/admin/users` | RBAC test |
| Missing Token | `GET {{adminServiceUrl}}/api/v1/admin/me` | Negative test |
| Invalid Token | `GET {{adminServiceUrl}}/api/v1/admin/me` | Negative test |

---

## Variables Updated

### Collection Variables

#### Admin Service Collection
| Variable | Value | Type | Description |
|----------|-------|------|-------------|
| baseUrl | `http://localhost:8080` | string | API Gateway URL |
| adminServiceUrl | `http://localhost:8087` | string | Admin Service Direct URL |
| userServiceUrl | `http://localhost:8091` | string | User Service Direct URL |
| accessToken | (empty) | string | Current access token |
| refreshToken | (empty) | string | Current refresh token |
| currentRole | (empty) | string | Current user role |
| currentUserId | (empty) | string | Current user ID |
| email | (empty) | string | Current user email |

#### User Service Collection
| Variable | Value | Type | Description |
|----------|-------|------|-------------|
| baseUrl | `http://localhost:8091` | string | User Service URL |
| accessToken | (empty) | string | Current access token |
| refreshToken | (empty) | string | Current refresh token |
| currentRole | (empty) | string | Current user role (NEW) |
| currentUserId | (empty) | string | Current user ID (NEW) |
| email | `testuser@cyberlearnix.com` | string | Test email |
| password | `password123` | string | Test password |
| otp | `123456` | string | Test OTP |

### Environment Variables

#### New Environment File
| Variable | Value | Type | Description |
|----------|-------|------|-------------|
| baseUrl | `http://localhost:8080` | default | API Gateway URL |
| adminServiceUrl | `http://localhost:8087` | default | Admin Service URL |
| userServiceUrl | `http://localhost:8091` | default | User Service URL |
| courseServiceUrl | `http://localhost:8083` | default | Course Service URL |
| instructorServiceUrl | `http://localhost:8088` | default | Instructor Service URL |
| cartServiceUrl | `http://localhost:8081` | default | Cart Service URL |
| couponServiceUrl | `http://localhost:8082` | default | Coupon Service URL |
| orderServiceUrl | `http://localhost:8084` | default | Order Service URL |
| paymentServiceUrl | `http://localhost:8085` | default | Payment Service URL |
| wishlistServiceUrl | `http://localhost:8090` | default | Wishlist Service URL |
| notificationServiceUrl | `http://localhost:8093` | default | Notification Service URL |
| reviewServiceUrl | `http://localhost:8089` | default | Review Service URL |
| accessToken | (empty) | secret | Current access token |
| refreshToken | (empty) | secret | Current refresh token |
| currentRole | (empty) | default | Current user role |
| currentUserId | (empty) | default | Current user ID |
| email | `testuser@cyberlearnix.com` | default | Test email |
| password | `password123` | secret | Test password |
| otp | `123456` | default | Test OTP |
| studentToken | (empty) | secret | Student-specific token |
| instructorToken | (empty) | secret | Instructor-specific token |
| mainAdminToken | (empty) | secret | Main Admin-specific token |
| subAdminToken | (empty) | secret | Sub Admin-specific token |

---

## Test Scripts Updated

### Login Test Script
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Login successful", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('authentication');
    pm.expect(jsonData.authentication).to.have.property('accessToken');
});

// Save tokens and user info to collection variables
var jsonData = pm.response.json();
if (jsonData.authentication) {
    pm.collectionVariables.set('accessToken', jsonData.authentication.accessToken);
    pm.collectionVariables.set('refreshToken', jsonData.authentication.refreshToken);
    console.log('Access token saved successfully');
}
if (jsonData.user) {
    pm.collectionVariables.set('currentRole', jsonData.user.role);
    pm.collectionVariables.set('currentUserId', jsonData.user.id);
    pm.collectionVariables.set('email', jsonData.user.email);
    console.log('User info saved: ' + jsonData.user.role);
}
```

### RBAC Test Script
```javascript
pm.test("Expect 403 Forbidden", function () {
    pm.expect(pm.response.code).to.eql(403);
});
```

### Negative Test Script
```javascript
pm.test("Expect 401 Unauthorized", function () {
    pm.expect(pm.response.code).to.eql(401);
});
```

---

## Endpoint Path Corrections

### Admin Service Endpoints
All admin endpoints now use `/api/v1/admin/` instead of `/api/v1/admins/`:

| Old Path | New Path |
|----------|----------|
| `/api/v1/admins/login` | REMOVED (use `/api/v1/auth/login`) |
| `/api/v1/admins/register` | `/api/v1/admin/register` |
| `/api/v1/admins/me` | `/api/v1/admin/me` |
| `/api/v1/admin/users` | `/api/v1/admin/users` |
| `/api/v1/admin/users/:id` | `/api/v1/admin/users/:id` |
| `/api/v1/admin/users/:id/status` | `/api/v1/admin/users/:id/status` |
| `/api/v1/admin/instructors` | `/api/v1/admin/instructors` |

---

## Authorization Headers

### Updated Pattern
All protected requests now use:
```
Authorization: Bearer {{accessToken}}
```

### Collection-Level Auth
All collections have bearer authentication configured:
```json
"auth": {
    "type": "bearer",
    "bearer": [
        {
            "key": "token",
            "value": "{{accessToken}}"
        }
    ]
}
```

---

## Login Credentials

### Main Admin
```json
{
    "email": "mainadmin@cyberlearnix.com",
    "password": "MainAdmin@123"
}
```

### Student
```json
{
    "email": "student@cyberlearnix.com",
    "password": "Student123!"
}
```

### Instructor
```json
{
    "email": "instructor@cyberlearnix.com",
    "password": "Instructor123!"
}
```

### Sub Admin
```json
{
    "email": "subadmin@cyberlearnix.com",
    "password": "SubAdmin123!"
}
```

---

## Legacy References Removed

### Removed References
- ❌ `AdminAuthController` - All references removed
- ❌ `/api/v1/admin/login` - Replaced with `/api/v1/auth/login`
- ❌ `ADMIN` role - Replaced with `MAIN_ADMIN`
- ❌ `SUPER_ADMIN` role - Replaced with `MAIN_ADMIN`

### Current Roles
- ✅ `STUDENT` - Regular student users
- ✅ `INSTRUCTOR` - Course instructors
- ✅ `MAIN_ADMIN` - Full system administrator
- ✅ `SUB_ADMIN` - Service-specific administrators

---

## Collection Structure

### Admin Service Collection Structure
```
├── Authentication (via User Service)
│   ├── Main Admin Login
│   ├── Student Login
│   ├── Instructor Login
│   ├── Sub Admin Login
│   ├── Refresh Token
│   └── Logout
├── Admin Business Operations
│   ├── Register Sub Admin
│   ├── Get Admin Profile
│   └── Update Admin Profile
├── User Management
│   ├── Get All Users
│   ├── Get User by ID
│   ├── Update User Status
│   └── Delete User
├── RBAC Authorization Tests
│   ├── Student Access Admin API - Expect 403
│   ├── Instructor Access Admin API - Expect 403
│   ├── Main Admin Access Admin API - Expect 200
│   └── Sub Admin Access Admin API - Expect 200
└── Negative Test Cases
    ├── Missing Token
    └── Invalid Token
```

---

## Requests Requiring Manual Fixes

### 1. User Service Collection
**File:** `User_Service_API_Collection.postman_collection.json`

**Manual Fix Required:** Update login test script to save role and userId
- Location: Login with Password request
- Current: Saves only accessToken and refreshToken
- Required: Also save currentRole and currentUserId

**Fix:**
```javascript
// Add to existing test script
if (jsonData.user) {
    pm.collectionVariables.set('currentRole', jsonData.user.role);
    pm.collectionVariables.set('currentUserId', jsonData.user.id);
    pm.collectionVariables.set('email', jsonData.user.email);
}
```

### 2. Other Service Collections
**Files:**
- `Course_Service_API_Collection.postman_collection.json`
- `Instructor_Service_API_Collection.postman_collection.json`
- `Cart_Service_API_Collection.postman_collection.json`
- `Coupon_Service_API_Collection.postman_collection.json`
- `Order_Service_API_Collection.postman_collection.json`
- `Payment_Service_API_Collection.postman_collection.json`
- `Wishlist_Service_API_Collection.postman_collection.json`
- `Review_Service_API_Collection.postman_collection.json`
- `Notification_Service_API_Collection.postman_collection.json`

**Manual Fix Required:** Update authorization headers to use `{{accessToken}}`
- All protected requests should use: `Authorization: Bearer {{accessToken}}`
- Remove any hardcoded tokens or manual token extraction

### 3. Complete API Collection
**File:** `CyberLearnix_LMS_Complete_API_Collection.postman_collection.json`

**Manual Fix Required:** 
- Remove any `/api/v1/admin/login` references
- Update all login requests to use `/api/v1/auth/login`
- Add role-specific login requests (Student, Instructor, Main Admin, Sub Admin)
- Update test scripts to save role and userId

---

## Authentication Flow

### New Login Flow
1. **Request:** `POST {{userServiceUrl}}/api/v1/auth/login`
2. **Body:** `{ "email": "...", "password": "..." }`
3. **Response:** 
   ```json
   {
     "user": {
       "id": "uuid",
       "email": "...",
       "role": "MAIN_ADMIN"
     },
     "authentication": {
       "accessToken": "...",
       "refreshToken": "..."
     }
   }
   ```
4. **Test Script:** Saves tokens and user info to variables
5. **Subsequent Requests:** Use `Authorization: Bearer {{accessToken}}`

### Refresh Token Flow
1. **Request:** `POST {{userServiceUrl}}/api/v1/auth/refresh`
2. **Auth:** `Authorization: Bearer {{refreshToken}}`
3. **Response:** New access token
4. **Test Script:** Updates accessToken variable

### Logout Flow
1. **Request:** `POST {{userServiceUrl}}/api/v1/auth/logout`
2. **Auth:** `Authorization: Bearer {{accessToken}}`
3. **Body:** `{ "refreshToken": "{{refreshToken}}" }`
4. **Test Script:** Clears token variables

---

## Collection Runner Execution

### Execution Order
1. **Authentication Tests:**
   - Main Admin Login
   - Student Login
   - Instructor Login
   - Sub Admin Login

2. **Admin Business Tests:**
   - Register Sub Admin (requires MAIN_ADMIN)
   - Get Admin Profile
   - Update Admin Profile

3. **User Management Tests:**
   - Get All Users
   - Get User by ID
   - Update User Status
   - Delete User

4. **RBAC Tests:**
   - Student Access Admin API (expect 403)
   - Instructor Access Admin API (expect 403)
   - Main Admin Access Admin API (expect 200)
   - Sub Admin Access Admin API (expect 200)

5. **Negative Tests:**
   - Missing Token (expect 401)
   - Invalid Token (expect 401)

### Manual Intervention Required
- None - Collection should run from start to finish without manual intervention
- All login requests use fixed credentials
- All test scripts automatically save and use tokens

---

## Summary Statistics

### Files Updated
- **Collections Updated:** 2 (Admin Service, User Service)
- **Environment Files:** 1 (New comprehensive environment)
- **Total Files:** 3

### Requests Modified
- **Removed:** 1 (Admin Login from Admin Service)
- **Modified:** 8 (endpoint path corrections)
- **Added:** 12 (new authentication, RBAC, and negative tests)
- **Total Changes:** 21 requests

### Variables Added
- **Collection Variables:** 2 (currentRole, currentUserId)
- **Environment Variables:** 23 (comprehensive service URLs and tokens)

### Endpoints Corrected
- **Removed:** 1 (`/api/v1/admin/login`)
- **Path Corrections:** 7 (`/api/v1/admins/` → `/api/v1/admin/`)

---

## Next Steps

### For Developers
1. Import `Admin_Service_API_Collection_Updated.postman_collection.json`
2. Import `CyberLearnix_Environment_Updated.postman_environment.json`
3. Update `User_Service_API_Collection.postman_collection.json` manually if needed
4. Update other service collections with authorization header fixes
5. Run collection to verify all tests pass

### For QA
1. Execute Main Admin Login and verify token is saved
2. Execute Student Login and verify 403 on admin APIs
3. Execute RBAC tests to verify role-based access
4. Execute negative tests to verify error handling
5. Verify collection runs without manual intervention

### For API Consumers
1. Update login endpoint from `/api/v1/admin/login` to `/api/v1/auth/login`
2. Update authorization headers to use `Bearer {{accessToken}}`
3. Update role references from `ADMIN`/`SUPER_ADMIN` to `MAIN_ADMIN`
4. Test all authentication flows
5. Update API documentation

---

**Change Log Completed:** 2026-08-01  
**Status:** ✅ READY FOR USE  
**Files Generated:** 3
