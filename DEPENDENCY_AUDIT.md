# LMS Microservices Dependency Audit Matrix

## Service Versions Overview

| Service | Spring Boot | Spring Cloud | JJWT | Java Toolchain | Dependency Management | Key Issues |
|---------|-------------|--------------|------|----------------|----------------------|------------|
| api-gateway | 3.2.5 | - | 0.12.3 | languageVersion(21) | 1.1.7 | JWT version mismatch |
| admin-service | 3.5.1 | - | 0.11.5 | languageVersion(21) | 1.1.7 | DevTools not scoped |
| user-service | 4.0.3 | - | 0.11.5 | languageVersion(21) | 1.1.7 | Latest Boot, H2 runtimeOnly |
| course-service | 3.5.1 | - | 0.11.5 | languageVersion(21) | 1.1.7 | DevTools, H2 testRuntimeOnly |
| coupon-service | 3.2.4 | - | 0.11.5 | languageVersion(21) | 1.1.7 | Oldest Boot version |
| notification-service | 3.2.5 | - | 0.11.5 | languageVersion(21) | 1.1.4 | Different dep management |
| cart-service | 3.4.3 | 2024.0.0 | 0.11.5 | languageVersion(21) | 1.1.7 | Has Spring Cloud |
| order-service | 3.4.3 | 2024.0.0 | 0.11.5 | languageVersion(21) | 1.1.7 | DevTools, Spring Cloud |
| payment-service | 3.2.5 | - | - | sourceCompatibility(21) | 1.1.4 | No JWT, old toolchain |
| instructor-service | 3.2.5 | - | 0.11.5 | languageVersion(21) | 1.1.7 | DevTools not scoped |
| review-service | 3.3.5 | 2023.0.3 | 0.11.5 | languageVersion(21) | 1.1.7 | Variable management |
| wishlist-service | 3.4.3 | 2024.0.0 | 0.11.5 | languageVersion(21) | 1.1.7 | Spring AI, Admin |
| commonlibs | - | - | 0.11.5 | languageVersion(21) | - | Shared JWT validator |

## Dependency Issues Found

### Critical Version Conflicts
1. **Spring Boot**: Versions range from 3.2.4 to 4.0.3 (user-service is way ahead)
2. **JJWT**: api-gateway uses 0.12.3, all others use 0.11.5
3. **Spring Cloud**: Inconsistent versions (2023.0.3 vs 2024.0.0)
4. **Dependency Management**: Mix of 1.1.4 and 1.1.7

### Redundant Dependencies
1. **spring-boot-starter-logging**: Explicitly in admin-service (redundant)
2. **commonlibs**: Declared twice in user-service
3. **JWT**: Individual service declarations when commonlibs has it

### Scope Issues
1. **H2 Database**: runtimeOnly in user-service, payment-service (should be testRuntimeOnly)
2. **DevTools**: In course-service, order-service, instructor-service (need developmentOnly scope)

### Java Toolchain Inconsistency
- Most services: `languageVersion = JavaLanguageVersion.of(21)`
- payment-service: `sourceCompatibility = JavaVersion.VERSION_21`

## Spring Boot Compatibility Analysis

### Current Version Distribution
- 3.2.x: 6 services (api-gateway, coupon-service, notification-service, payment-service, instructor-service)
- 3.3.x: 1 service (review-service)
- 3.4.x: 3 services (cart-service, order-service, wishlist-service)
- 3.5.x: 2 services (admin-service, course-service)
- 4.0.x: 1 service (user-service)

### Recommendation
Standardize on **Spring Boot 3.4.3** for the following reasons:
- Already used by 3 services (cart, order, wishlist)
- Compatible with Java 21
- Stable release with good security updates
- Supports current Spring Cloud 2024.0.0
- Reasonable upgrade path for 3.2.x and 3.3.x services
- Downgrade needed for user-service (4.0.3 → 3.4.3) and admin/course (3.5.1 → 3.4.3)

## JJWT Compatibility Analysis

### Current Usage
- 0.11.5: Used by 11 services + commonlibs
- 0.12.3: Used only by api-gateway

### Recommendation
Standardize on **JJWT 0.12.3** for the following reasons:
- Latest stable version with security improvements
- api-gateway already uses it successfully
- Better Java 21 support
- Breaking changes are minimal from 0.11.5
- commonlibs can be upgraded to match

## Spring Cloud Compatibility

### Current Usage
- 2023.0.3: review-service
- 2024.0.0: cart-service, order-service, wishlist-service

### Recommendation
Standardize on **Spring Cloud 2024.0.0** (compatible with Spring Boot 3.4.x)

## Other Key Dependencies to Standardize

### SpringDoc OpenAPI
- Most services: 2.3.0
- review-service: 2.6.0 (variable)
- Recommendation: 2.3.0 (stable, widely used)

### Spring Security
- All using Spring Boot starter security
- Recommendation: Keep as managed by Spring Boot BOM

### Lombok
- Most services: using compileOnly + annotationProcessor
- Recommendation: Keep current pattern

## Build Configuration Issues

### Missing
- Root gradle.properties for version management
- Gradle build optimization settings
- Centralized dependency version management

### Gradle Version
- Current: 8.14 (modern, good)
- Recommendation: Keep current version

## Docker Configuration Issues

### Current State
- All services use similar single-stage Dockerfiles
- eclipse-temurin:21-jre-alpine base
- IST timezone configuration
- No health checks
- No multi-stage builds
- Running as root

### Recommendations
- Implement multi-stage builds for smaller images
- Add health checks
- Run as non-root user
- Optimize layer caching
- Standardize base image strategy

## Runtime Performance Issues

### JWT Filter
- Extensive logging on every request
- Multiple regex operations
- Sequential pattern matching
- Complex nested conditionals

### Recommendations
- Reduce logging verbosity
- Optimize path matching with compiled patterns
- Cache regex operations
- Consider async logging for production

## Implementation Priority

### Phase 1 - Dependency Consistency (Critical)
1. Create gradle.properties with centralized versions
2. Standardize Spring Boot to 3.4.3
3. Standardize JJWT to 0.12.3
4. Standardize Spring Cloud to 2024.0.0
5. Fix duplicate commonlibs in user-service
6. Remove redundant spring-boot-starter-logging
7. Fix H2 scopes (runtimeOnly → testRuntimeOnly)
8. Fix DevTools scopes (add developmentOnly)

### Phase 2 - Build Optimization
9. Standardize Java toolchain to languageVersion approach
10. Add Gradle build optimizations to gradle.properties
11. Centralize common dependency versions

### Phase 3 - Docker Optimization
12. Implement multi-stage builds
13. Add non-root user execution
14. Add health checks
15. Optimize layer caching

### Phase 4 - Runtime Optimization
16. Optimize JWT filter performance
17. Reduce unnecessary logging
18. Review connection pools (only if measurable issues)
