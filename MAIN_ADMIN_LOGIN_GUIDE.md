# Main Admin Login Guide

## Overview

The CyberLearnix LMS system has a unified authentication system where both regular users and admins login through the **User Service**. The system automatically routes admin login requests to the Admin Service for validation.

## Main Admin Credentials

### Default Credentials (Development)
- **Email:** `mainadmin@cyberlearnix.com`
- **Password:** `MainAdmin@123`
- **Role:** MAIN_ADMIN
- **Assigned Service:** ALL (full system access)

### Configuration Location
The main admin credentials are configured in:
- **File:** `admin-service/src/main/resources/application.properties`
- **Properties:**
  ```properties
  main.admin.email=mainadmin@cyberlearnix.com
  main.admin.password=MainAdmin@123
  main.admin.first-name=Main
  main.admin.last-name=Admin
  main.admin.mobile-number=+911234567890
  ```

### Bootstrap Process
The main admin is automatically created/updated on service startup via:
- **Class:** `MainAdminBootstrap.java`
- **Purpose:** Ensures main admin exists with correct credentials and permissions
- **Database:** `lms_admin_db` table `admins`

---

## Login Methods

### Method 1: Unified Login (Recommended)

#### Endpoint
```
POST http://localhost:8091/api/v1/auth/login
```

#### Request Body
```json
{
  "email": "mainadmin@cyberlearnix.com",
  "password": "MainAdmin@123"
}
```

#### Process Flow
1. User Service receives login request
2. User Service checks if email exists in `users` table
3. If not found, forwards to Admin Service at `/api/v1/admin/login`
4. Admin Service validates credentials in `admins` table
5. Admin Service returns admin user data
6. User Service generates JWT tokens with MAIN_ADMIN role
7. Returns unified login response with tokens

#### Example Response
```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "id": "uuid",
    "email": "mainadmin@cyberlearnix.com",
    "firstName": "Main",
    "lastName": "Admin",
    "mobileNumber": "+911234567890",
    "role": "MAIN_ADMIN",
    "adminType": "MAIN_ADMIN",
    "assignedService": "ALL",
    "permissions": ["READ", "WRITE", "DELETE", "MANAGE_USERS", "MANAGE_COURSES", "MANAGE_INSTRUCTORS", "VIEW_ANALYTICS", "SYSTEM_CONFIG"],
    "verified": true,
    "approved": true
  },
  "authentication": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresIn": "15 minutes",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshTokenExpiresIn": "30 days"
  },
  "sessionInfo": {
    "loginTime": "2026-08-01T21:30:00",
    "ipAddress": "127.0.0.1",
    "device": "Mozilla/5.0..."
  },
  "timestamp": "2026-08-01T21:30:00"
}
```

### Method 2: Direct Admin Service Login (Internal Use Only)

#### Endpoint
```
POST http://localhost:8087/api/v1/admin/login
```

#### Request Body
```json
{
  "email": "mainadmin@cyberlearnix.com",
  "password": "MainAdmin@123"
}
```

#### Notes
- This endpoint is for internal service-to-service communication
- Does not generate JWT tokens (User Service handles token generation)
- Used by User Service when routing admin login requests
- **Not recommended for direct client use**

---

## Login Testing

### Using PowerShell
```powershell
$headers = @{"Content-Type" = "application/json"}
$body = @{
    email = "mainadmin@cyberlearnix.com"
    password = "MainAdmin@123"
} | ConvertTo-Json -Depth 10

$response = Invoke-WebRequest -Uri "http://localhost:8091/api/v1/auth/login" -Method POST -Headers $headers -Body $body -UseBasicParsing
$response.Content
```

### Using cURL
```bash
curl -X POST http://localhost:8091/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"mainadmin@cyberlearnix.com","password":"MainAdmin@123"}'
```

### Using Postman
1. Create new POST request
2. URL: `http://localhost:8091/api/v1/auth/login`
3. Headers: `Content-Type: application/json`
4. Body (raw JSON):
   ```json
   {
     "email": "mainadmin@cyberlearnix.com",
     "password": "MainAdmin@123"
   }
   ```
5. Send request

---

## Main Admin Permissions

### Full System Access
As MAIN_ADMIN, you have access to:
- ✅ User Management (view, update, delete, status change)
- ✅ Course Management (approve, reject, update)
- ✅ Instructor Management (approve, reject applications)
- ✅ Order Management (view, process)
- ✅ Payment Management (view, process)
- ✅ Review Management (moderate)
- ✅ Coupon Management (create, update, delete)
- ✅ Admin Management (create sub-admins)
- ✅ System Configuration
- ✅ Analytics and Reports

### API Endpoints Access
Main Admin can access all admin endpoints:
- `/api/v1/admin/users/*` - User management
- `/api/v1/admin/courses/*` - Course management
- `/api/v1/admin/instructors/*` - Instructor management
- `/api/v1/admin/orders/*` - Order management
- `/api/v1/admin/payments/*` - Payment management
- `/api/v1/admin/reviews/*` - Review management
- `/api/v1/admin/coupons/*` - Coupon management
- `/api/v1/admin/register` - Create sub-admins

---

## Security Features

### Account Status
- **Approval Status:** APPROVED (auto-approved)
- **Verification Status:** Verified (auto-verified)
- **Account Type:** MAIN_ADMIN
- **Assigned Service:** ALL (no restrictions)

### Password Security
- Password is BCrypt hashed in database
- Default password should be changed in production
- Minimum password requirements apply

### JWT Token Claims
Main Admin tokens include:
```json
{
  "userId": "admin-uuid",
  "email": "mainadmin@cyberlearnix.com",
  "role": "MAIN_ADMIN",
  "adminType": "MAIN_ADMIN",
  "assignedService": "ALL",
  "type": "access"
}
```

---

## Troubleshooting

### Login Fails with "Invalid Credentials"
1. Verify admin service is running on port 8087
2. Check database connection to `lms_admin_db`
3. Verify main admin exists in `admins` table
4. Check admin service logs for authentication errors
5. Verify password matches configuration

### Login Fails with "Account Not Approved"
1. Main admin should be auto-approved on bootstrap
2. Check `MainAdminBootstrap` logs
3. Manually update in database:
   ```sql
   UPDATE admins 
   SET approval_status = 'APPROVED', verified = true 
   WHERE email = 'mainadmin@cyberlearnix.com';
   ```

### User Service Cannot Connect to Admin Service
1. Verify admin service URL configuration:
   ```properties
   admin.service.url=http://localhost:8087
   ```
2. Check network connectivity between services
3. Verify admin service is running
4. Check firewall rules

### Token Generation Fails
1. Verify JWT secret matches between services
2. Check JWT configuration in both services
3. Verify token expiration settings
4. Check issuer and audience configuration

---

## Production Setup

### Change Default Credentials
1. Update `application.properties`:
   ```properties
   main.admin.email=your-admin@yourdomain.com
   main.admin.password=YourSecurePassword123!
   ```
2. Or use environment variables:
   ```bash
   export MAIN_ADMIN_EMAIL=your-admin@yourdomain.com
   export MAIN_ADMIN_PASSWORD=YourSecurePassword123!
   ```
3. Restart admin service to apply changes

### Security Best Practices
1. Change default password before production deployment
2. Use strong password (min 12 characters, mixed case, numbers, symbols)
3. Enable 2FA for main admin (if implemented)
4. Use environment variables for sensitive data
5. Rotate passwords regularly
6. Monitor admin login attempts
7. Implement IP whitelisting for admin access
8. Enable audit logging for admin actions

---

## Sub Admin Creation

Main Admin can create sub-admins with limited permissions:

### Endpoint
```
POST http://localhost:8087/api/v1/admin/register
```

### Required Headers
```
Authorization: Bearer {main-admin-jwt-token}
```

### Request Body
```json
{
  "email": "subadmin@cyberlearnix.com",
  "password": "SubAdmin@123",
  "confirmPassword": "SubAdmin@123",
  "firstName": "Sub",
  "lastName": "Admin",
  "mobileNumber": "+919876543210",
  "alternateMobileNumber": "+919876543211",
  "assignedService": "COURSE_SERVICE"
}
```

### Available Services
- `ALL` - Full access (MAIN_ADMIN only)
- `USER_SERVICE` - User management
- `COURSE_SERVICE` - Course management
- `INSTRUCTOR_SERVICE` - Instructor management
- `ORDER_SERVICE` - Order management
- `PAYMENT_SERVICE` - Payment management
- `REVIEW_SERVICE` - Review management
- `COUPON_SERVICE` - Coupon management
- `CART_SERVICE` - Cart management
- `WISHLIST_SERVICE` - Wishlist management
- `NOTIFICATION_SERVICE` - Notification management

---

## Summary

### Quick Login Command
```bash
curl -X POST http://localhost:8091/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"mainadmin@cyberlearnix.com","password":"MainAdmin@123"}'
```

### Key Points
- ✅ Login through User Service unified endpoint
- ✅ Auto-bootstrap of main admin on service startup
- ✅ Full system access as MAIN_ADMIN
- ✅ JWT tokens with MAIN_ADMIN role claims
- ✅ Can create sub-admins with limited permissions
- ✅ Change default credentials for production

### Services Required
- ✅ User Service (port 8091) - Handles login and token generation
- ✅ Admin Service (port 8087) - Validates admin credentials
- ✅ PostgreSQL databases (lms_user_db, lms_admin_db)

---

**Last Updated:** 2026-08-01  
**Service Version:** Spring Boot 3  
**Status:** ✅ Configured and Ready
