
package com.swachvega.apigateway.filter;

import com.swachvega.apigateway.security.SimpleJwtTokenProvider;

import io.jsonwebtoken.JwtException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SimpleJwtTokenProvider jwtTokenProvider;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(

            // Admin service
            "/api/v1/admin/register",

            // User service - authentication and social login
            "/api/v1/auth/login",
            "/api/v1/auth/login/**",
            "/api/v1/auth/login/otp/resend",
            "/api/v1/auth/password/otp/resend",
            "/api/v1/auth/register",
            "/api/v1/auth/register/email",
            "/api/v1/auth/register/resend-otp",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/refresh",
            "/api/v1/auth/password/forgot",
            "/api/v1/auth/password/verify-otp",
            "/api/v1/auth/password/reset",
            "/api/v1/auth/otp/resend",
            "/api/v1/auth/social-login",
            "/api/v1/auth/social-login/**",
            "/api/v1/auth/oauth/**",
            "/oauth2/**",
            "/login/oauth2/**",
            "/api/v1/users/login/social",

            // Course service - public browsing
            "/api/v1/courses",
            "/api/v1/courses/*",
            "/api/v1/courses/*/sections",
            "/api/v1/sections/*/lectures",
            "/api/v1/courses/*/preview",
            "/api/v1/courses/*/students",
            "/api/v1/courses/*/impressions",

            // Internal enrollment
            "/api/v1/enrollments/internal/enroll",

            // Review service
            "/api/v1/reviews/course/**",

            // Actuator and health
            "/actuator/**",
            "/health/**",

            // Swagger and OpenAPI
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/userservice/swagger-ui/**",
            "/userservice/v3/api-docs/**",
            "/courseservice/swagger-ui/**",
            "/courseservice/v3/api-docs/**",
            "/cartservice/swagger-ui/**",
            "/cartservice/v3/api-docs/**",
            "/couponservice/swagger-ui/**",
            "/couponservice/v3/api-docs/**",
            "/wishlistservice/swagger-ui/**",
            "/wishlistservice/v3/api-docs/**",
            "/adminservice/swagger-ui/**",
            "/adminservice/v3/api-docs/**",

            // Security verification
            "/api/v1/security/verify-activity",
            "/api/v1/security/confirm-activity",
            "/api/v1/security/report-compromised"
    );

    public JwtAuthenticationFilter(
            SimpleJwtTokenProvider jwtTokenProvider,
            ReactiveRedisTemplate<String, Object> redisTemplate) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String rawPath = exchange.getRequest().getPath().value();
        String path = normalizePath(rawPath);
        String method = exchange.getRequest().getMethod().name();

        log.info(
                "Processing request: {} {} (normalized: {}) - Checking if public path",
                method, rawPath, path
        );

        // Skip authentication for OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("OPTIONS preflight request detected: {}", path);
            return chain.filter(exchange);
        }

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            log.info("Public path detected: {}", path);
            return chain.filter(exchange);
        }

        log.info("Protected path detected: {}", path);

        // Extract Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        log.info(
                "Authorization header for path {}: {}",
                path,
                authHeader != null ? "Bearer ***" : "missing"
        );

        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);

        // Token fingerprint
        String tokenFingerprint = token != null
                ? token.substring(0, Math.min(8, token.length()))
                : "null";

        int tokenLength = token != null ? token.length() : 0;

        log.info(
                "Token fingerprint: {}, length: {} for path: {}",
                tokenFingerprint, tokenLength, path
        );

        if (token == null || token.isEmpty()) {
            log.warn("No valid token found for protected path: {}", path);

            return unauthorizedResponse(
                    exchange,
                    "Missing or invalid authorization token"
            );
        }

        log.info("Token extracted successfully for path: {}", path);

        // Check Redis blacklist
        String blacklistKey = "blacklist:token:" + token;

        return redisTemplate.hasKey(blacklistKey)

                .flatMap(isBlacklisted -> {

                    if (Boolean.TRUE.equals(isBlacklisted)) {

                        log.warn(
                                "Token is blacklisted: {}",
                                token.substring(0, Math.min(10, token.length()))
                                        + "..."
                        );

                        return unauthorizedResponse(
                                exchange,
                                "Token has been logged out"
                        );
                    }

                    // Validate JWT access token
                    return jwtTokenProvider.validateAccessToken(token)

                            .doOnNext(claims -> {

                                log.info(
                                        "Token validated successfully for user: {} on path: {}",
                                        claims.get("sub"), path
                                );

                                log.info(
                                        "Gateway validated token fingerprint: {}, issuer: {}, audience: {}, subject: {}",
                                        tokenFingerprint,
                                        claims.get("iss"),
                                        claims.get("aud"),
                                        claims.get("sub")
                                );
                            })

                            .flatMap(claims -> {

                                // Add user information to request headers
                                ServerWebExchange modifiedExchange =
                                        addUserHeaders(exchange, claims);

                                log.info(
                                        "Proceeding to downstream service for path: {}",
                                        path
                                );

                                return chain.filter(modifiedExchange);
                            });
                })

                .switchIfEmpty(Mono.defer(() -> {

                    if (exchange.getResponse().isCommitted()) {
                        return Mono.empty();
                    }

                    log.warn(
                            "Token validation returned empty result for path: {}",
                            path
                    );

                    return unauthorizedResponse(
                            exchange,
                            "Invalid or expired token"
                    );
                }))

                .onErrorResume(ex -> {

                    if (exchange.getResponse().isCommitted()) {
                        return Mono.empty();
                    }

                    // JWT-related errors
                    if (ex instanceof JwtException) {

                        log.warn(
                                "Token validation failed for path {}: {}",
                                path,
                                ex.getMessage()
                        );

                        return unauthorizedResponse(
                                exchange,
                                "Invalid or expired token: " + ex.getMessage()
                        );
                    }

                    // Other errors
                    log.error(
                            "Downstream error after authentication for path {}: {}",
                            path,
                            ex.getMessage(),
                            ex
                    );

                    return Mono.error(ex);
                });
    }

    // Check whether the path is public
    private boolean isPublicPath(String path) {

        String normalized = normalizePath(path);

        for (String pattern : PUBLIC_PATHS) {

            if (matchesPublicPattern(normalized, pattern)) {
                return true;
            }
        }

        return false;
    }

    // Match public path patterns
    private boolean matchesPublicPattern(String path, String pattern) {

        String normalizedPattern = normalizePath(pattern);

        if (normalizedPattern.endsWith("/**")) {

            String prefix = normalizedPattern.substring(
                    0,
                    normalizedPattern.length() - 3
            );

            return path.equals(prefix)
                    || path.startsWith(prefix + "/");
        }

        if (normalizedPattern.contains("*")) {

            String[] patternParts = normalizedPattern.split("/");
            String[] pathParts = path.split("/");

            if (patternParts.length != pathParts.length) {
                return false;
            }

            for (int i = 0; i < patternParts.length; i++) {

                if ("*".equals(patternParts[i])) {
                    continue;
                }

                if (!patternParts[i].equals(pathParts[i])) {
                    return false;
                }
            }

            return true;
        }

        return path.equals(normalizedPattern);
    }

    // Normalize request paths
    private String normalizePath(String p) {

        if (p == null || p.isEmpty()) {
            return "/";
        }

        String normalized = p.replaceAll("/{2,}", "/");

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String stripTrailingSlash(String s) {

        if (s == null || s.isEmpty() || "/".equals(s)) {
            return "/";
        }

        if (s.endsWith("/") && s.length() > 1) {
            return s.substring(0, s.length() - 1);
        }

        return s;
    }

    // FIXED METHOD: Add user headers using a mutable copy
    private ServerWebExchange addUserHeaders(
            ServerWebExchange exchange,
            Map<String, Object> claims) {

        // IMPORTANT: Never modify the original read-only headers.
        // Create a separate mutable copy.
        HttpHeaders headers = new HttpHeaders();

        headers.putAll(exchange.getRequest().getHeaders());

        // Resolve subject
        String sub = claims.get("sub") != null
                ? String.valueOf(claims.get("sub"))
                : null;

        String phoneNumber = claims.get("phoneNumber") != null
                ? String.valueOf(claims.get("phoneNumber"))
                : null;

        String mobileNumber = claims.get("mobileNumber") != null
                ? String.valueOf(claims.get("mobileNumber"))
                : null;

        String userIdClaim = claims.get("userId") != null
                ? String.valueOf(claims.get("userId"))
                : null;

        String merchantIdClaim = claims.get("merchantId") != null
                ? String.valueOf(claims.get("merchantId"))
                : null;

        // Backward compatibility
        if ((merchantIdClaim == null || merchantIdClaim.isBlank())
                && claims.get("registrationId") != null) {

            merchantIdClaim = String.valueOf(
                    claims.get("registrationId")
            );
        }

        // Resolve user ID
        String resolvedUserId =
                (sub != null && !sub.isBlank()) ? sub
                        : (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber
                        : (mobileNumber != null && !mobileNumber.isBlank()) ? mobileNumber
                        : "";

        if (userIdClaim != null && !userIdClaim.isBlank()) {
            resolvedUserId = userIdClaim;
        }

        // Resolve username
        String usernameClaim = claims.get("username") != null
                ? String.valueOf(claims.get("username"))
                : null;

        if ("null".equalsIgnoreCase(usernameClaim)) {
            usernameClaim = null;
        }

        String emailClaim = claims.get("email") != null
                ? String.valueOf(claims.get("email"))
                : null;

        if ("null".equalsIgnoreCase(emailClaim)) {
            emailClaim = null;
        }

        String resolvedUsername =
                (usernameClaim != null && !usernameClaim.isBlank()) ? usernameClaim
                        : (emailClaim != null && !emailClaim.isBlank()) ? emailClaim
                        : (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber
                        : (mobileNumber != null && !mobileNumber.isBlank()) ? mobileNumber
                        : (!resolvedUserId.isBlank()) ? resolvedUserId
                        : null;

        // Determine best role
        String bestRole = null;

        Object rolesClaim = claims.get("roles");

        if (rolesClaim == null) {
            rolesClaim = claims.get("authorities");
        }

        if (rolesClaim instanceof String rs) {

            String up = rs.toUpperCase();

            if (up.contains("MAIN_ADMIN")) {
                bestRole = "MAIN_ADMIN";
            } else if (up.contains("SUB_ADMIN")) {
                bestRole = "SUB_ADMIN";
            }

        } else if (rolesClaim instanceof java.util.Collection<?> col) {

            for (Object r : col) {

                if (r == null) {
                    continue;
                }

                String up = String.valueOf(r).toUpperCase();

                if (up.contains("MAIN_ADMIN")) {
                    bestRole = "MAIN_ADMIN";
                    break;
                }

                if (up.contains("SUB_ADMIN")) {
                    bestRole = "SUB_ADMIN";
                }
            }
        }

        if (bestRole == null && claims.get("role") != null) {
            bestRole = String.valueOf(claims.get("role"));
        }

        // Add basic user information
        headers.set("X-User-Id", resolvedUserId);

        headers.set(
                "X-Username",
                resolvedUsername != null ? resolvedUsername : ""
        );

        headers.set(
                "X-User-Role",
                bestRole != null
                        ? bestRole
                        : String.valueOf(claims.get("role"))
        );

        headers.set(
                "X-Session-Id",
                String.valueOf(claims.get("sessionId"))
        );

        // Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && !authHeader.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        }

        // Merchant information
        if (merchantIdClaim != null && !merchantIdClaim.isBlank()) {
            headers.set("X-Merchant-Id", merchantIdClaim);
        }

        // Additional user information
        if (claims.get("email") != null) {
            headers.set(
                    "X-User-Email",
                    String.valueOf(claims.get("email"))
            );
        }

        if (claims.get("fullName") != null) {
            headers.set(
                    "X-User-FullName",
                    String.valueOf(claims.get("fullName"))
            );
        }

        // Admin information
        if (claims.get("adminId") != null) {
            headers.set(
                    "X-Admin-Id",
                    String.valueOf(claims.get("adminId"))
            );
        }

        // Phone information
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            headers.set("X-User-Phone", phoneNumber);

        } else if (mobileNumber != null && !mobileNumber.isBlank()) {
            headers.set("X-User-Phone", mobileNumber);
        }

        // Token metadata
        if (claims.get("iat") != null) {
            headers.set(
                    "X-Token-IssuedAt",
                    String.valueOf(claims.get("iat"))
            );
        }

        if (claims.get("exp") != null) {
            headers.set(
                    "X-Token-ExpiresAt",
                    String.valueOf(claims.get("exp"))
            );
        }

        if (claims.get("jti") != null) {
            headers.set(
                    "X-Token-Id",
                    String.valueOf(claims.get("jti"))
            );
        }

        // Create a decorated request with the updated headers.
        // The returned headers are read-only, but already contain
        // all the changes made to our separate mutable copy.
        ServerHttpRequest mutatedRequest =
                new ServerHttpRequestDecorator(exchange.getRequest()) {

                    @Override
                    public HttpHeaders getHeaders() {
                        return HttpHeaders.readOnlyHttpHeaders(headers);
                    }
                };

        return exchange.mutate()
                .request(mutatedRequest)
                .build();
    }

    // Check admin role
    private boolean hasAdminRole(Map<String, Object> claims) {

        Object roles = claims.get("roles");

        if (roles == null) {
            roles = claims.get("authorities");
        }

        if (roles == null) {
            roles = claims.get("role");
        }

        if (roles == null) {
            return false;
        }

        if (roles instanceof String s) {

            String normalized = s.toUpperCase();

            if (normalized.contains("MAIN_ADMIN")
                    || normalized.contains("SUB_ADMIN")) {
                return true;
            }

            for (String part : normalized.split("[ ,]")) {

                if ("MAIN_ADMIN".equals(part)
                        || "SUB_ADMIN".equals(part)) {
                    return true;
                }
            }

            return false;
        }

        if (roles instanceof java.util.Collection<?> col) {

            for (Object r : col) {

                if (r != null) {

                    String ru = String.valueOf(r).toUpperCase();

                    if ("MAIN_ADMIN".equals(ru)
                            || "SUB_ADMIN".equals(ru)
                            || ru.contains("MAIN_ADMIN")
                            || ru.contains("SUB_ADMIN")) {
                        return true;
                    }
                }

                if (r instanceof Map<?, ?> m) {

                    Object v = m.get("authority");

                    if (v != null) {

                        String vu = String.valueOf(v).toUpperCase();

                        if (vu.contains("MAIN_ADMIN")
                                || vu.contains("SUB_ADMIN")) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        return false;
    }

    // Unauthorized response
    private Mono<Void> unauthorizedResponse(
            ServerWebExchange exchange,
            String message) {

        return errorResponse(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                message
        );
    }

    // Forbidden response
    private Mono<Void> forbiddenResponse(
            ServerWebExchange exchange,
            String message) {

        return errorResponse(
                exchange,
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                message
        );
    }

    // Common error response
    private Mono<Void> errorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            String code,
            String message) {

        log.warn("{}: {}", code, message);

        var response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.empty();
        }

        String traceId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Trace-Id");

        if (traceId == null || traceId.isBlank()) {
            traceId = java.util.UUID.randomUUID().toString();
        }

        response.setStatusCode(status);

        response.getHeaders().set(
                "Content-Type",
                "application/json"
        );

        response.getHeaders().set("X-Trace-Id", traceId);

        String escapedMessage = message
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");

        String errorBody = String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"code\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"traceId\":\"%s\"}",
                java.time.Instant.now(),
                status.value(),
                code,
                escapedMessage,
                exchange.getRequest().getPath().value(),
                traceId
        );

        var buffer = response.bufferFactory().wrap(
                errorBody.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
