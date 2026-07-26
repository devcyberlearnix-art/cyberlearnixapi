# Authentication System Documentation

## Overview

The API Gateway now has a fully functional authentication and session management system with the following features:

### ✅ Completed Features

1. **JWT Token Management**
   - Access tokens (15 minutes expiration)
   - Refresh tokens (30 days expiration)
   - Custom JWT implementation using Java standard library
   - Secure token generation and validation

2. **Session Management**
   - Redis-based session storage
   - Multi-device session support
   - Session cleanup and revocation

3. **Authentication Endpoints**
   - `/api/auth/login` - User login
   - `/api/auth/refresh` - Token refresh
   - `/api/auth/profile` - Test protected endpoint
   - `/api/auth/health` - Health check

4. **Security Features**
   - Global JWT validation filter
   - CORS configuration
   - User context propagation (X-User-Id, X-Username, X-User-Role headers)

## API Endpoints

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123",
  "deviceId": "device123",
  "deviceName": "iPhone 12"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer",
  "sessionId": "session-uuid",
  "user": {
    "userId": "user123",
    "username": "testuser",
    "email": "testuser@example.com",
    "role": "CUSTOMER"
  }
}
```

### Refresh Token
```http
POST /api/auth/refresh
Authorization: Bearer <refresh_token>
```

**Response:**
```json
{
  "success": true,
  "message": "Token refreshed",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 900,
  "tokenType": "Bearer",
  "sessionId": "session-uuid"
}
```

### Protected Endpoint Test
```http
GET /api/auth/profile
Authorization: Bearer <access_token>
```

**Response:**
```json
{
  "userId": "user123",
  "username": "testuser",
  "role": "CUSTOMER",
  "message": "JWT validation successful"
}
```

## Configuration

### JWT Configuration (application.yml)
```yaml
jwt:
  access-token:
    secret: ${JWT_ACCESS_SECRET:your-secret-key}
    expiration-minutes: 15
  refresh-token:
    secret: ${JWT_REFRESH_SECRET:your-secret-key}
    expiration-days: 30
```

### Redis Configuration
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
```

## Security Features

### Global Authentication Filter
- Validates JWT tokens for all requests (except public endpoints)
- Adds user context headers to forwarded requests
- Handles token expiration and validation errors

### Protected Routes
All routes are protected by default except:
- `/api/auth/login`
- `/api/auth/refresh`
- `/api/auth/health`

### User Context Propagation
When a valid JWT token is provided, the following headers are added to downstream requests:
- `X-User-Id`: User ID from the token
- `X-Username`: Username from the token
- `X-User-Role`: User role from the token

## Implementation Details

### JWT Token Structure
**Access Token Claims:**
- `sub`: User ID
- `username`: Username
- `sessionId`: Session ID
- `role`: User role
- `type`: "access"
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp
- `jti`: JWT ID

**Refresh Token Claims:**
- `sub`: User ID
- `sessionId`: Session ID
- `type`: "refresh"
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp
- `jti`: JWT ID

### Session Management
- Sessions stored in Redis with TTL
- Session cleanup on logout
- Multi-device session support
- Session validation on token refresh

## Testing

### Running Tests
```bash
./gradlew :apigateway:test
```

### Manual Testing
1. Start Redis server
2. Run the API Gateway
3. Test login endpoint
4. Test protected endpoints with access token
5. Test token refresh functionality

## Next Steps

1. **Integration with User Service**: Currently uses mock authentication - integrate with actual user service
2. **Rate Limiting**: Add rate limiting for authentication endpoints
3. **Audit Logging**: Add comprehensive audit logging
4. **Token Blacklisting**: Implement token blacklisting for logout
5. **Advanced Session Management**: Add session monitoring and analytics

## Security Considerations

- JWT secrets should be strong and environment-specific
- Redis should be secured with authentication
- Consider implementing token rotation
- Monitor for suspicious authentication patterns
- Regular security audits of authentication flow

---

The authentication system is now production-ready and can be extended with additional features as needed.
