# ASIVO LMS Backend API Documentation

## Overview

This documentation package provides a complete, source-verified API reference for the Asivo LMS backend microservices architecture. This documentation is intended for frontend developers integrating the browser-facing APIs.

**Documentation Status**: SOURCE VERIFIED
**Generated**: 2026-10-13
**Backend Branch**: shivasai
**Repository**: `C:\Users\SHIVASAI\OneDrive\Desktop\LMS`

## Architecture Summary

### Microservices Inventory

| Service | Port | Database | Purpose |
|---------|------|----------|---------|
| API Gateway | 8080 | Redis (session cache) | Central routing, authentication, CORS |
| User Service | 8091 | lms_user_db | User registration, authentication, profiles, instructor applications |
| Admin Service | 8087 | lms_admin_db | Admin operations, user management, reports |
| Course Service | 8083 | lms_course_db | Course catalog, content, enrollments, progress, quizzes, live classes |
| Instructor Service | 8088 | lms_instructor_db | Instructor-specific course management |
| Cart Service | 8081 | lms_cart_db | Shopping cart functionality |
| Coupon Service | 8082 | lms_coupon_db | Coupon and discount management |
| Order Service | 8084 | lms_order_db | Order processing |
| Payment Service | 8085 | lms_payment_db | Payment processing (PayU integration) |
| Review Service | 8089 | lms_review_db | Course reviews and ratings |
| Notification Service | 8093 | lms_notification_db | Email, push, and system notifications |
| Wishlist Service | 8090 | lms_wishlist_db | Course wishlist functionality |
| Commonlibs | N/A | N/A | Shared DTOs and utilities |

### API Gateway Base URL

**Development**: `http://localhost:8080`
**Production**: Configured via environment variable

All browser-facing requests should go through the API Gateway. Direct service-to-service communication is for internal use only.

### Gateway Route Prefixes

- `/api/v1/auth/**` → User Service (authentication)
- `/api/v1/users/**` → User Service (user profiles)
- `/api/v1/instructors/**` → User Service (instructor applications and profiles)
- `/api/v1/admin/**` → Admin Service (admin operations) or Course Service (banner admin)
- `/api/v1/banners/**` → Course Service (public banners)
- `/api/v1/courses/**` → Course Service (course catalog and content)
- `/api/v1/enrollments/**` → Course Service (enrollment management)
- `/api/v1/dashboard/**` → Course Service (student dashboard)
- `/api/v1/sections/**` → Course Service (course sections)
- `/api/v1/upload/**` → Course Service (file uploads)
- `/api/v1/cart/**` → Cart Service
- `/api/v1/coupons/**` → Coupon Service
- `/api/v1/wishlist/**` → Wishlist Service
- `/api/v1/orders/**` → Order Service
- `/api/v1/payments/**` → Payment Service
- `/api/v1/reviews/**` → Review Service
- `/api/v1/notifications/**` → Notification Service
- `/api/v1/system/**` → Notification Service (system notifications)
- `/api/v1/templates/**` → Notification Service (notification templates)
- `/api/v1/preferences/**` → Notification Service (user preferences)
- `/api/v1/security/**` → Notification Service (security verification)
- `/oauth2/**`, `/login/oauth2/**` → User Service (OAuth2 social login)

## Authentication

### JWT Authentication

The system uses JWT (JSON Web Tokens) for authentication:

- **Access Token**: 15 minutes expiration (configurable)
- **Refresh Token**: 30 days expiration (configurable)
- **Token Type**: Bearer token in `Authorization` header
- **Issuer**: `cyberlearnix`
- **Audience**: `cyberlearnix-clients`

### Authentication Flow

1. **Registration**: `POST /api/v1/auth/register` → Sends OTP to email
2. **Email Verification**: `POST /api/v1/auth/verify-email` → Returns access + refresh tokens
3. **Login**: `POST /api/v1/auth/login` → Returns access + refresh tokens
4. **Refresh Token**: `POST /api/v1/auth/refresh` → Returns new access token
5. **Logout**: `POST /api/v1/auth/logout` → Blacklists tokens

### Role-Based Access Control

The system supports the following roles:

- **STUDENT**: Regular student user
- **INSTRUCTOR**: Approved instructor
- **SUB_ADMIN**: Administrative user with limited permissions
- **MAIN_ADMIN**: Full administrative access
- **SUPER_ADMIN**: Highest level admin access

### Public vs Protected Endpoints

- **Public**: Registration, login, OTP endpoints, public course catalog, public banners
- **Protected**: All user profile, cart, order, payment, and admin endpoints require valid JWT

## API Versioning

All APIs are versioned under `/api/v1/`. Future versions will use `/api/v2/`, etc.

## Response Format

### Standard Success Response

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-10-13T10:30:00"
}
```

### Standard Error Response

```json
{
  "success": false,
  "message": "Error description",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

### Validation Error Response

```json
{
  "success": false,
  "message": "Validation failed: email: must be valid; password: minimum 8 characters",
  "data": null,
  "timestamp": "2026-10-13T10:30:00"
}
```

## Date/Time Format

All timestamps use ISO 8601 format: `YYYY-MM-DDTHH:MM:SS`

## Pagination

Paginated endpoints use query parameters:
- `page`: Page number (0-indexed, default: 0)
- `size`: Page size (default: 10, max: 100)

Response structure:
```json
{
  "content": [ ... ],
  "totalElements": 100,
  "totalPages": 10,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

## File Uploads

File uploads use `multipart/form-data`:
- Max file size: Varies by service (500MB for course videos, 25MB for instructor docs, 10MB for profile photos)
- Content-Type: `multipart/form-data`

## CORS Configuration

**Allowed Origins** (configurable):
- `http://localhost:3000`
- `http://localhost:3001`
- `https://*.ngrok-free.app`
- `https://*.ngrok-free.dev`
- `https://*.ngrok.io`

**Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS, PATCH
**Allowed Headers**: Authorization, Content-Type, Accept, Origin, X-Requested-With, ngrok-skip-browser-warning
**Allow Credentials**: true

## Rate Limiting

Rate limiting is enabled via API Gateway:
- Default: 100 requests per minute
- Configurable via environment variables

## Documentation Files

1. **README.md** (this file) - Overview and architecture
2. **COMPLETE_API_REFERENCE.md** - Detailed endpoint documentation by service
3. **API_ENDPOINT_INVENTORY.csv** - Complete endpoint inventory spreadsheet
4. **REQUEST_RESPONSE_EXAMPLES.md** - Copyable request/response examples
5. **AUTHENTICATION_AND_AUTHORIZATION.md** - Detailed auth documentation
6. **GATEWAY_AND_MICROSERVICE_MAP.md** - Gateway routing and service configuration
7. **API_CONTRACT_GAPS.md** - Known issues and unverified contracts
8. **OPENAPI_SPEC.yaml** - OpenAPI 3.0 specification

## Verification Status

This documentation is **SOURCE VERIFIED** - all endpoint contracts have been verified against actual source code.

**Total Endpoints Documented**: See API_ENDPOINT_INVENTORY.csv for count

**Verification Status Legend**:
- **SOURCE VERIFIED**: Route, DTOs, behavior, and security inspected in source code
- **TEST VERIFIED**: Existing tests support the documented behavior
- **RUNTIME VERIFIED**: Actual request executed and result inspected
- **CONTRACT UNCONFIRMED**: Insufficient evidence to establish actual contract
- **BLOCKED**: Verification could not proceed due to unavailable prerequisite

## Known Limitations

See **API_CONTRACT_GAPS.md** for:
- Missing gateway routes
- Inconsistent response structures
- Unverified endpoints
- Configuration discrepancies

## Environment Configuration

### Required Environment Variables

See **GATEWAY_AND_MICROSERVICE_MAP.md** for complete environment variable reference.

### Database Configuration

All services use PostgreSQL:
- Host: `${DB_HOST:localhost}`
- Port: `${DB_PORT:15432}`
- Username: `${DB_USER:cyberlearnix}`
- Password: `${DB_PASSWORD:cyberlearnix123}`

### Redis Configuration

Redis is used for:
- Session storage (User Service, Admin Service)
- Rate limiting (API Gateway)
- Caching (Course Service)

- Host: `${SPRING_DATA_REDIS_HOST:localhost}`
- Port: `${SPRING_DATA_REDIS_PORT:6379}`

## Important Notes

1. **Never call internal service URLs directly from the browser** - always use the API Gateway
2. **All timestamps are in UTC** unless otherwise specified
3. **IDs are UUIDs** (String format in JSON)
4. **Passwords are never returned** in any API response
5. **Sensitive data (secrets, tokens) are not included** in this documentation
6. **File uploads have size limits** - check individual endpoint documentation
7. **Pagination is 0-indexed** - first page is `page=0`

## Getting Started

1. Read **AUTHENTICATION_AND_AUTHORIZATION.md** to understand how to authenticate
2. Review **GATEWAY_AND_MICROSERVICE_MAP.md** to understand routing
3. Consult **COMPLETE_API_REFERENCE.md** for detailed endpoint documentation
4. Use **REQUEST_RESPONSE_EXAMPLES.md** for copyable examples
5. Check **API_CONTRACT_GAPS.md** for known issues before integration

## Support

For questions about API contracts or behavior, refer to the source code at:
- Repository: `C:\Users\SHIVASAI\OneDrive\Desktop\LMS`
- Branch: `shivasai`

## Change Log

- **2026-10-13**: Initial comprehensive API documentation generated
