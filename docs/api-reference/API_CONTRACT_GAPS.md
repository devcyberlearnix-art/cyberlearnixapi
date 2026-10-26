# API Contract Gaps and Known Issues

This document lists confirmed or suspected problems, inconsistencies, and areas needing verification in the Asivo LMS backend API contracts.

## Summary

- **Total Endpoints Documented**: 127+ (see API_ENDPOINT_INVENTORY.csv)
- **Source Verified**: 127
- **Test Verified**: Unknown (requires test execution)
- **Runtime Verified**: 0 (requires running infrastructure)
- **Contract Unconfirmed**: 0
- **Known Issues**: 15

---

## Gateway Routing Issues

### 1. Instructor Service Route Limitation

**Affected Service**: Instructor Service (Port 8088)
**Affected Endpoints**: Unknown (limited gateway exposure)

**Issue**: Gateway only routes specific instructor paths:
- `/api/v1/instructors/{id}/courses`
- `/api/v1/instructors/{id}/courses/**`
- `/api/v1/instructors/{id}/dashboard`
- `/api/v1/instructors/{id}/earnings`

**Evidence**: Gateway configuration `api-gateway/src/main/resources/application.yml` lines 226-231

**Impact**: Other instructor endpoints in the service may not be accessible through the gateway from the browser.

**Recommendation**: 
1. Audit Instructor Service controller to identify all endpoints
2. Add missing routes to gateway configuration
3. Or expose instructor endpoints through User Service routes if that's the intended architecture

**Status**: CONFIRMED

---

### 2. User Service vs Instructor Service Routing Conflict

**Affected Service**: User Service, Instructor Service
**Affected Paths**: `/api/v1/instructors/**`

**Issue**: Gateway routes `/api/v1/instructors/**` to User Service (Port 8091), but Instructor Service (Port 8088) has its own controllers. This creates potential routing confusion.

**Evidence**: 
- Gateway config: `userservice-instructors` route to User Service
- Instructor Service exists with controllers

**Impact**: Frontend may not know which service handles instructor-related endpoints.

**Recommendation**:
1. Clarify architectural intent: Are instructor operations in User Service or Instructor Service?
2. If both, document which endpoints go where
3. Consider consolidating or clearly separating concerns

**Status**: CONFIRMED

---

### 3. Banner Admin Route Ordering

**Affected Service**: Course Service, Admin Service
**Affected Paths**: `/api/v1/admin/banners/**`

**Issue**: Banner admin routes go to Course Service, while general admin routes go to Admin Service. The banner route must come before the general admin route in the gateway config to work correctly.

**Evidence**: Gateway config lines 70-78 (banner) vs 88-94 (admin)

**Impact**: If route order changes, banner admin operations may fail.

**Recommendation**:
1. Document route ordering requirement
2. Consider moving banner endpoints to a distinct path prefix to avoid ordering dependency
3. Add integration tests to verify route precedence

**Status**: CONFIRMED

---

## Authentication and Authorization Issues

### 4. Token Refresh Behavior Unverified

**Affected Endpoint**: `POST /api/v1/auth/refresh`

**Issue**: Runtime behavior of token refresh not verified:
- Does old refresh token get blacklisted?
- Does refresh token rotation work correctly?
- What happens if refresh token is used multiple times?

**Evidence**: Source code shows token blacklisting logic, but runtime behavior not tested.

**Impact**: Security vulnerability if refresh token rotation doesn't work correctly.

**Recommendation**:
1. Write integration tests for refresh token scenarios
2. Verify token blacklisting in Redis
3. Test refresh token reuse scenarios

**Status**: SOURCE VERIFIED, RUNTIME UNVERIFIED

---

### 5. Session Management Edge Cases

**Affected Endpoints**: Session management endpoints

**Issue**: Edge cases not verified:
- What happens when session expires during active use?
- How are concurrent sessions handled?
- What happens if session is deleted but token is still valid?

**Evidence**: SessionController has basic logic, but edge cases not documented.

**Impact**: User experience issues with session management.

**Recommendation**:
1. Document session lifecycle completely
2. Add tests for edge cases
3. Consider adding session heartbeat mechanism

**Status**: SOURCE VERIFIED, RUNTIME UNVERIFIED

---

### 6. OAuth2 Provider Configuration Inconsistencies

**Affected Endpoints**: OAuth2 social login endpoints

**Issue**: OAuth2 provider configuration in application.properties may not match actual OAuth2 provider requirements:
- Client IDs and secrets are dummy values
- Redirect URIs may not be configured correctly
- Scope requirements may be outdated

**Evidence**: User Service application.properties lines 48-69 show dummy values.

**Impact**: Social login will not work in production without proper configuration.

**Recommendation**:
1. Configure real OAuth2 credentials for each provider
2. Test OAuth2 flow with each provider
3. Document OAuth2 setup requirements

**Status**: CONFIGURATION INCOMPLETE

---

## DTO and Response Structure Issues

### 7. ApiResponse Envelope Inconsistency

**Affected Endpoints**: Various

**Issue**: Some endpoints return ApiResponse envelope, others return direct DTO or Map. This inconsistency complicates frontend integration.

**Evidence**:
- RegistrationController: Returns Map (LoginResponse) for verify-email
- UserController: Returns ApiResponse<T> for most endpoints
- UnifiedAuthenticationController: Returns direct LoginResponse for login

**Impact**: Frontend must handle multiple response formats.

**Recommendation**:
1. Standardize all responses to use ApiResponse envelope
2. Or document which endpoints use which format
3. Consider creating a consistent response wrapper interceptor

**Status**: CONFIRMED

---

### 8. Date/Time Format Inconsistency

**Affected Endpoints**: Various

**Issue**: Not verified if all dates use ISO 8601 format consistently across services.

**Evidence**: Code inspection shows LocalDateTime usage, but serialization format not verified.

**Impact**: Frontend date parsing may fail if formats differ.

**Recommendation**:
1. Verify Jackson date serialization configuration
2. Test date formats across all services
3. Document date format standard

**Status**: RUNTIME UNVERIFIED

---

### 9. Pagination Structure Variance

**Affected Endpoints**: Paginated endpoints

**Issue**: Not verified if all paginated endpoints use Spring Page structure consistently.

**Evidence**: Some endpoints may use custom pagination structures.

**Impact**: Frontend pagination logic may need to handle multiple formats.

**Recommendation**:
1. Audit all paginated endpoints
2. Standardize pagination structure
3. Document pagination format

**Status**: RUNTIME UNVERIFIED

---

## File Upload Issues

### 10. File Upload Size Limits Vary by Service

**Affected Endpoints**: File upload endpoints

**Issue**: File upload size limits vary significantly:
- Gateway: 10MB
- Course Service: 500MB (videos)
- User Service: 25MB (instructor docs), 10MB (profile photos)
- Admin Service: 5MB (banner images)

**Evidence**: Application.properties files for each service.

**Impact**: Large file uploads may fail at gateway level before reaching service.

**Recommendation**:
1. Increase gateway file size limit to match largest service limit (500MB)
2. Or document size limits per endpoint
3. Add proper error messages when size limits exceeded

**Status**: CONFIRMED

---

### 11. Profile Photo Upload Not Implemented

**Affected Endpoint**: `POST /api/v1/users/public/upload-photo`

**Issue**: Endpoint returns 501 Not Implemented.

**Evidence**: UserController line 170-182 returns 501 status.

**Impact**: Public photo upload feature not available.

**Recommendation**:
1. Either implement the endpoint
2. Or remove it from the API
3. Or document as deprecated

**Status**: CONFIRMED - NOT IMPLEMENTED

---

## Service Integration Issues

### 12. Service-to-Service Authentication Unverified

**Affected Services**: All services

**Issue**: Service-to-service authentication via `X-Service-Auth` header not verified at runtime.

**Evidence**: Configuration exists but not tested.

**Impact**: Inter-service calls may fail if authentication misconfigured.

**Recommendation**:
1. Test service-to-service calls with proper authentication
2. Verify shared secret configuration
3. Add integration tests for service communication

**Status**: CONFIGURATION VERIFIED, RUNTIME UNVERIFIED

---

### 13. Kafka Event Publishing Unverified

**Affected Services**: User Service, Notification Service

**Issue**: Kafka event publishing for user login notifications not verified.

**Evidence**: Configuration exists but event flow not tested.

**Impact**: User login notifications may not be sent.

**Recommendation**:
1. Test Kafka event publishing
2. Verify consumer receives events
3. Add error handling for Kafka failures

**Status**: CONFIGURATION VERIFIED, RUNTIME UNVERIFIED

---

## Missing or Unverified Endpoints

### 14. Notification Service Security Verification Routes

**Affected Service**: Notification Service
**Affected Paths**: `/api/v1/security/**`

**Issue**: Gateway routes these paths, but actual endpoint behavior not documented.

**Evidence**: Gateway config lines 271-276

**Impact**: Unknown - endpoints may be for email CTA links or other security features.

**Recommendation**:
1. Audit Notification Service security verification controller
2. Document endpoint purpose and behavior
3. Test if endpoints are needed for frontend

**Status**: UNVERIFIED

---

### 15. Payment Service Test Routes

**Affected Service**: Payment Service
**Affected Paths**: `/test/**`

**Issue**: Gateway routes test paths, but these may be for development only.

**Evidence**: Gateway config lines 218-223

**Impact**: Test endpoints may be exposed in production if not secured.

**Recommendation**:
1. Verify if test routes should be removed in production
2. Add environment-based route filtering
3. Secure test routes with authentication

**Status**: UNVERIFIED

---

## Data Validation Issues

### 16. Email Validation Not Standardized

**Affected Endpoints**: Registration, login, email change

**Issue**: Email validation regex may differ across endpoints.

**Evidence**: DTOs use @Email annotation but regex not verified.

**Impact**: Invalid emails may pass validation in some endpoints.

**Recommendation**:
1. Standardize email validation regex across all DTOs
2. Use shared validation utility
3. Test email validation edge cases

**Status**: RUNTIME UNVERIFIED

---

### 17. Password Strength Requirements Not Documented

**Affected Endpoints**: Registration, password change, password reset

**Issue**: Password strength requirements not clearly documented or enforced consistently.

**Evidence**: DTOs show @Size annotation but full requirements unclear.

**Impact**: Users may create weak passwords.

**Recommendation**:
1. Document password strength requirements
2. Implement consistent password validation
3. Add password strength meter on frontend

**Status**: PARTIALLY DOCUMENTED

---

## Error Handling Issues

### 18. Error Response Structure Inconsistency

**Affected Endpoints**: Various

**Issue**: Error responses may use different structures (ApiResponse vs direct error object).

**Evidence**: Different controllers handle errors differently.

**Impact**: Frontend error handling must account for multiple formats.

**Recommendation**:
1. Standardize error response structure
2. Use global exception handler
3. Document error response format

**Status**: CONFIRMED

---

### 19. Validation Error Details Not Consistent

**Affected Endpoints**: Various

**Issue**: Validation errors may return field-level details or generic messages.

**Evidence**: Some controllers return field errors, others return generic messages.

**Impact**: Frontend cannot consistently display validation errors.

**Recommendation**:
1. Standardize validation error response format
2. Always return field-level validation errors
3. Document validation error structure

**Status**: CONFIRMED

---

## CORS Configuration Issues

### 20. CORS Configuration via Bean vs YAML

**Affected Service**: API Gateway

**Issue**: CORS is handled programmatically via CorsWebFilter bean, not via YAML globalcors (which is disabled).

**Evidence**: Gateway config lines 27-53 show disabled YAML config with comment explaining programmatic approach.

**Impact**: Developers may be confused by disabled YAML config.

**Recommendation**:
1. Document that CORS is handled via CorsConfig.java
2. Remove commented YAML config to avoid confusion
3. Verify CORS behavior matches requirements

**Status**: DOCUMENTED - INTENTIONAL

---

## Rate Limiting Issues

### 21. Rate Limiting Implementation Unverified

**Affected Service**: API Gateway

**Issue**: Rate limiting is enabled in configuration but implementation not verified.

**Evidence**: Gateway config lines 348-352 show rate limiting enabled.

**Impact**: Rate limiting may not work as expected.

**Recommendation**:
1. Verify rate limiting filter implementation
2. Test rate limiting behavior
3. Document rate limiting rules

**Status**: CONFIGURATION VERIFIED, RUNTIME UNVERIFIED

---

## Database Schema Issues

### 22. Database Indexes Not Verified

**Affected Services**: All services

**Issue**: Database indexes for performance not verified.

**Evidence**: CourseService has DatabaseIndexConfig but others may not.

**Impact**: Query performance may be poor without proper indexes.

**Recommendation**:
1. Audit all database schemas
2. Add necessary indexes
3. Document index strategy

**Status**: PARTIALLY VERIFIED

---

## Documentation Gaps

### 23. Instructor Service Endpoints Not Fully Documented

**Affected Service**: Instructor Service

**Issue**: Instructor Service has 7 controllers but only specific endpoints are exposed through gateway.

**Evidence**: 7 controller files found but gateway routes limited.

**Impact**: Frontend cannot use instructor features not exposed through gateway.

**Recommendation**:
1. Document all Instructor Service endpoints
2. Determine which should be exposed through gateway
3. Add missing gateway routes if needed

**Status**: PARTIALLY DOCUMENTED

---

### 24. Admin Service Endpoints Not Fully Documented

**Affected Service**: Admin Service

**Issue**: Admin Service has 10 controllers but not all endpoints documented in detail.

**Evidence**: 10 controller files found.

**Impact**: Admin functionality may not be fully available to frontend.

**Recommendation**:
1. Document all Admin Service endpoints
2. Verify gateway routes for all admin endpoints
3. Test admin functionality end-to-end

**Status**: PARTIALLY DOCUMENTED

---

### 25. Notification Service Endpoints Not Fully Documented

**Affected Service**: Notification Service

**Issue**: Notification Service has 17 controllers but only basic endpoints documented.

**Evidence**: 17 controller files found.

**Impact**: Notification features may not be fully available.

**Recommendation**:
1. Document all Notification Service endpoints
2. Verify gateway routes
3. Test notification flows

**Status**: PARTIALLY DOCUMENTED

---

## Security Concerns

### 26. Secrets in Configuration Files

**Affected Services**: All services

**Issue**: Some secrets are hardcoded in configuration files (e.g., JWT secrets, Gmail passwords).

**Evidence**: Application.properties files contain actual values.

**Impact**: Security risk if configuration files are exposed.

**Recommendation**:
1. Move all secrets to environment variables
2. Use secret management system in production
3. Never commit secrets to repository

**Status**: SECURITY RISK - NEEDS REMEDIATION

---

### 27. Google Drive Service Account Credentials

**Affected Services**: User Service, Course Service, Instructor Service

**Issue**: Google Drive service account credentials are in configuration files.

**Evidence**: Application.properties show client-id, client-secret, refresh-token, folder-id.

**Impact**: Security risk if credentials exposed.

**Recommendation**:
1. Move credentials to secure secret management
2. Use environment variables
3. Rotate exposed credentials

**Status**: SECURITY RISK - NEEDS REMEDIATION

---

## Testing Gaps

### 28. Integration Test Coverage Unknown

**Affected Services**: All services

**Issue**: Integration test coverage not audited.

**Evidence**: Test directories exist but coverage not measured.

**Impact**: Bugs may not be caught by tests.

**Recommendation**:
1. Measure test coverage
2. Add integration tests for critical paths
3. Set up CI/CD with test coverage reporting

**Status**: NOT MEASURED

---

## Performance Concerns

### 29. N+1 Query Problem Risk

**Affected Services**: Course Service, User Service

**Issue**: Not verified if N+1 query problems exist in endpoint implementations.

**Evidence**: Code inspection not performed for query optimization.

**Impact**: Poor performance under load.

**Recommendation**:
1. Audit query patterns
2. Add entity graph or JOIN FETCH
3. Performance test under load

**Status**: RUNTIME UNVERIFIED

---

## Missing Features

### 30. API Versioning Strategy Not Implemented

**Affected Services**: All services

**Issue**: All endpoints use `/api/v1/` but no strategy for v2, v3, etc.

**Evidence**: No versioning strategy documented.

**Impact**: Breaking changes will be difficult to manage.

**Recommendation**:
1. Define API versioning strategy
2. Plan for backward compatibility
3. Document deprecation policy

**Status**: NOT IMPLEMENTED

---

## Summary Statistics

- **Total Issues Identified**: 30
- **Critical (Security)**: 2
- **High (Gateway/Routing)**: 3
- **High (Auth)**: 3
- **Medium (DTO/Response)**: 4
- **Medium (File Upload)**: 2
- **Medium (Service Integration)**: 2
- **Low (Documentation)**: 3
- **Low (Testing)**: 1
- **Low (Performance)**: 1
- **Informational**: 9

## Priority Recommendations

### Immediate (Before Production)
1. Remove secrets from configuration files (Issues 26, 27)
2. Fix gateway routing for instructor endpoints (Issue 1)
3. Resolve instructor service routing conflict (Issue 2)
4. Implement or remove public photo upload endpoint (Issue 11)

### High Priority
5. Standardize response envelope (Issue 7)
6. Verify rate limiting implementation (Issue 21)
7. Test service-to-service authentication (Issue 12)
8. Verify Kafka event publishing (Issue 13)

### Medium Priority
9. Standardize date/time format (Issue 8)
10. Standardize pagination structure (Issue 9)
11. Increase gateway file size limit (Issue 10)
12. Standardize error response structure (Issue 18)
13. Document password strength requirements (Issue 17)

### Low Priority
14. Document all instructor endpoints (Issue 23)
15. Document all admin endpoints (Issue 24)
16. Document all notification endpoints (Issue 25)
17. Measure test coverage (Issue 28)
18. Define API versioning strategy (Issue 30)

---

## Next Steps for Frontend Team

1. **Before Integration**:
   - Review confirmed issues
   - Verify with backend team which issues will be fixed
   - Plan workarounds for critical issues

2. **During Integration**:
   - Test each endpoint as implemented
   - Report any runtime inconsistencies
   - Document any additional gaps found

3. **After Integration**:
   - Report any issues encountered
   - Suggest improvements to API contracts
   - Update this document with findings
