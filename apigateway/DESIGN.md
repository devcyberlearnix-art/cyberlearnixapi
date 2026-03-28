# API Gateway - Design Document

## Overview
The API Gateway is the single entry point for all client applications (mobile, web) to access the SwachVega ecommerce platform. It provides centralized authentication, request routing, load balancing, and cross-cutting concerns like logging and rate limiting.

## Purpose
- **Unified Entry Point**: Single facade for all backend microservices
- **Authentication & Authorization**: JWT-based session management and validation
- **Request Routing**: Intelligent routing to appropriate microservices
- **Security**: Token validation, CORS, and security headers
- **Protocol Translation**: HTTP to WebSocket for real-time services

## Port
- **Default**: 8080

## Technology Stack
- **Spring Cloud Gateway**: Reactive gateway with WebFlux
- **Spring Security**: Authentication and authorization
- **Redis**: Session storage and caching
- **JWT**: Stateless authentication tokens
- **WebSocket**: Real-time communication support

## Architecture

### Core Components

#### 1. **Route Configuration** (`application.yml`)
Declarative routing rules mapping URL patterns to backend services:
- `/api/users/**` → UserService
- `/api/consumer/auth/**` → UserService (Auth endpoints)
- `/api/products/**` → ProductService
- `/api/stores/**` → StoreService
- `/api/orders/**` → OrderService
- `/api/cart/**` → CartService
- `/api/search/**` → SearchService
- `/api/landing/**` → LandingPageService
- `/api/merchant/**` → MerchantService
- `/api/admin/**` → AdminService
- `/api/merchant/coupons/**` → CouponService
- `/ws`, `/token`, `/conversations/**` → ChatService (WebSocket)
- `/api/notifications/**` → NotificationService

#### 2. **AuthController**
- **POST `/api/gateway/auth/login`**: User login (phone + OTP)
- **POST `/api/gateway/auth/refresh`**: Refresh JWT access token
- **POST `/api/gateway/auth/logout`**: Invalidate session
- **POST `/api/gateway/auth/register`**: New user registration

Delegates authentication to UserService and manages JWT token lifecycle.

#### 3. **SessionManagementController**
- **GET `/api/gateway/session/info`**: Get current session details
- **POST `/api/gateway/session/validate`**: Validate JWT token
- **GET `/api/gateway/session/devices`**: List active devices for user
- **DELETE `/api/gateway/session/devices/{deviceId}`**: Revoke device session

#### 4. **AuthService**
Core authentication logic:
- Communicates with UserService for credential verification
- Generates JWT access and refresh tokens
- Stores session metadata in Redis
- Validates tokens on incoming requests

**Key Methods**:
- `login(PhoneOtpValidationDTO)`: Authenticate user and issue tokens
- `refreshToken(String refreshToken)`: Issue new access token
- `validateToken(String token)`: Verify JWT signature and expiration
- `logout(String userId)`: Invalidate all sessions for user

#### 5. **SessionService**
Manages user session lifecycle:
- Creates session records in Redis
- Tracks device information (browser, OS, IP)
- Handles multi-device sessions
- Session expiration and cleanup

#### 6. **JWT Utility**
- Token generation with configurable expiration
- Token parsing and validation
- Claims extraction (userId, roles, deviceId)

### Request Flow

#### Authentication Flow
```
Client → API Gateway → AuthController.login()
                     ↓
                  AuthService.login()
                     ↓
         [WebClient] → UserService /api/users/auth/validate-otp
                     ↓
         Generate JWT (access + refresh tokens)
                     ↓
         Store session in Redis
                     ↓
         Return tokens to client
```

#### Authenticated Request Flow
```
Client (with JWT in Authorization header)
   ↓
API Gateway - Global Filter
   ↓
Extract JWT from header
   ↓
Validate JWT signature & expiration
   ↓
Check session in Redis (optional)
   ↓
Add user context to request headers
   ↓
Route to target microservice
   ↓
Return response to client
```

#### Token Refresh Flow
```
Client (with refresh token)
   ↓
POST /api/gateway/auth/refresh
   ↓
Validate refresh token
   ↓
Check session in Redis
   ↓
Generate new access token
   ↓
Return new access token
```

### Security Features

#### 1. **JWT Token Structure**
**Access Token**:
- Subject: userId
- Claims: roles, deviceId, sessionId
- Expiration: 15 minutes (configurable)
- Algorithm: HS256 (HMAC with SHA-256)

**Refresh Token**:
- Subject: userId
- Claims: sessionId, deviceId
- Expiration: 30 days (configurable)
- Algorithm: HS256

#### 2. **Session Management**
- Sessions stored in Redis with TTL
- Support for multiple concurrent sessions per user
- Device fingerprinting for security
- Automatic session cleanup on expiration

#### 3. **CORS Configuration**
- Configured allowed origins
- Credential support for cookies
- Exposed headers for custom headers

#### 4. **Rate Limiting** (Future)
- Per-user rate limits
- Per-IP rate limits
- Per-endpoint rate limits

### Configuration

#### Key Properties (`application.yml`)
```yaml
server:
  port: 8080

spring:
  cloud:
    gateway:
      routes: [...]
      httpclient:
        response-timeout: 300s
        max-header-size: 16KB
  codec:
    max-in-memory-size: 30MB

jwt:
  access-token:
    secret: ${JWT_ACCESS_TOKEN_SECRET}
    expiration-minutes: 15
  refresh-token:
    secret: ${JWT_REFRESH_TOKEN_SECRET}
    expiration-days: 30
```

### Filters

#### 1. **Global Authentication Filter**
- Applied to all routes except public endpoints
- Validates JWT on every request
- Adds user context to request headers for downstream services

#### 2. **Logging Filter**
- Logs all incoming requests
- Logs response status and latency
- Correlation ID for request tracing

#### 3. **CORS Filter**
- Handles preflight OPTIONS requests
- Adds CORS headers to responses

## Design Patterns

### 1. **Gateway Pattern**
Single entry point for all client requests, abstracting the complexity of microservices architecture.

### 2. **Token-Based Authentication**
Stateless authentication using JWT, eliminating server-side session storage (except for revocation checks).

### 3. **Backend for Frontend (BFF)**
Gateway can be customized for different client types (mobile vs web) with client-specific endpoints.

### 4. **Circuit Breaker** (Future)
Resilience pattern to handle downstream service failures gracefully.

## Data Models

### Session Data (Redis)
```json
{
  "sessionId": "uuid",
  "userId": "123",
  "deviceId": "device-fingerprint",
  "deviceInfo": {
    "userAgent": "Mozilla/5.0...",
    "ipAddress": "192.168.1.1",
    "deviceType": "mobile"
  },
  "accessToken": "jwt-token",
  "refreshToken": "jwt-token",
  "createdAt": "2026-01-22T10:00:00Z",
  "expiresAt": "2026-01-22T10:15:00Z"
}
```

## Error Handling

### Error Response Structure
```json
{
  "timestamp": "2026-01-22T10:00:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "path": "/api/orders"
}
```

### Common Error Scenarios
- **401 Unauthorized**: Missing, invalid, or expired JWT
- **403 Forbidden**: Valid token but insufficient permissions
- **404 Not Found**: Route not configured
- **429 Too Many Requests**: Rate limit exceeded
- **502 Bad Gateway**: Downstream service unavailable
- **504 Gateway Timeout**: Downstream service timeout

## Performance Considerations

### 1. **Connection Pooling**
- HTTP client connection pools for downstream services
- Redis connection pooling for session storage

### 2. **Caching**
- Cache public endpoints (categories, products) at gateway level
- Cache user roles and permissions in Redis

### 3. **Async Processing**
- Spring WebFlux for non-blocking I/O
- Reactive programming model for high concurrency

### 4. **Resource Limits**
- Max in-memory buffer size: 30MB
- Response timeout: 300s
- Connection timeout: 20s

## Monitoring & Observability

### Metrics (Future)
- Request rate by endpoint
- Response time (p50, p95, p99)
- Error rate by status code
- Active sessions count
- JWT validation success/failure rate

### Logging
- Structured JSON logging
- Correlation ID for distributed tracing
- Request/response logging with sanitized sensitive data

### Health Checks
- Liveness probe: `/actuator/health/liveness`
- Readiness probe: `/actuator/health/readiness`
- Checks downstream service availability

## Testing Strategy

### Unit Tests
- JWT generation and validation
- Session service logic
- Route matching rules

### Integration Tests
- End-to-end authentication flow
- Token refresh flow
- Session management operations
- Route forwarding to downstream services

### Security Tests
- Token tampering detection
- Expired token rejection
- Invalid signature detection
- CSRF protection

## Deployment

### Environment Variables
```
JWT_ACCESS_TOKEN_SECRET=<secret>
JWT_REFRESH_TOKEN_SECRET=<secret>
JWT_ACCESS_TOKEN_EXPIRATION_MINUTES=15
JWT_REFRESH_TOKEN_EXPIRATION_DAYS=30
USERSERVICE_URL=http://userservice:8080
REDIS_HOST=redis
REDIS_PORT=6379
```

### Resource Requirements
- CPU: 0.5 cores
- Memory: 320MB
- Replicas: 2+ (for high availability)

## Future Enhancements

1. **Rate Limiting**: Implement distributed rate limiting with Redis
2. **Circuit Breaker**: Add Resilience4j for fault tolerance
3. **API Versioning**: Support multiple API versions (v1, v2)
4. **GraphQL Gateway**: Add GraphQL endpoint for flexible queries
5. **Request Transformation**: Transform requests/responses between versions
6. **OAuth2 Support**: Support OAuth2/OIDC for third-party authentication
7. **mTLS**: Mutual TLS for service-to-service communication
8. **WAF Integration**: Web Application Firewall for advanced security
9. **Analytics**: Track API usage patterns and user behavior
10. **A/B Testing**: Route traffic to different service versions for testing

## Dependencies
- Spring Cloud Gateway
- Spring Security
- Spring Data Redis
- Spring Boot Actuator
- JJWT (Java JWT library)
- WebFlux
- Lombok

## Security Best Practices
1. **Rotate JWT secrets regularly**
2. **Use HTTPS in production** (TLS termination at gateway)
3. **Implement token blacklisting** for compromised tokens
4. **Validate all input** before forwarding to services
5. **Sanitize logs** to prevent sensitive data leakage
6. **Use short-lived access tokens** (15 minutes)
7. **Implement refresh token rotation** for enhanced security
8. **Monitor for suspicious patterns** (multiple failed logins, token reuse)
