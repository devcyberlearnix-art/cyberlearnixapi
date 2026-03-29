package com.swachvega.apigateway.filter;

import com.swachvega.apigateway.security.SimpleJwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SimpleJwtTokenProvider jwtTokenProvider;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            // User service – auth (login, register, OTP, password reset)
            "/auth/",
            "/auth/login",
            "/auth/register",
            "/auth/forgot-password",
            "/auth/reset-password",
            "/auth/verify-email",
            "/auth/refresh-token",
            "/auth/resend-otp",
            "/auth/send-otp",
            "/auth/verify-otp",
            // Course service – public course browsing
            "/courses/",
            // Actuator / health
            "/actuator/",
            "/health/",
            // Swagger / OpenAPI docs
            "/swagger-ui/",
            "/swagger-ui.html",
            "/v3/api-docs/",
            "/userservice/swagger-ui/",
            "/userservice/v3/api-docs/",
            "/courseservice/swagger-ui/",
            "/courseservice/v3/api-docs/",
            "/cartservice/swagger-ui/",
            "/cartservice/v3/api-docs/",
            "/couponservice/swagger-ui/",
            "/couponservice/v3/api-docs/"
    );

    public JwtAuthenticationFilter(SimpleJwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String rawPath = exchange.getRequest().getPath().value();
        String path = normalizePath(rawPath);
        String method = exchange.getRequest().getMethod().name();
        
        log.info("Processing request: {} {} (normalized: {}) - Checking if public path", method, rawPath, path);
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            log.info("Public path detected, skipping authentication: {}", path);
            return chain.filter(exchange);
        }
        
        log.info("Protected path detected, checking authentication: {}", path);
        
        // Extract token from Authorization header
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);
        
        log.info("Authorization header for path {}: {}", path, authHeader != null ? "Bearer ***" : "missing");
        
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        
        if (token == null || token.isEmpty()) {
            log.warn("No valid token found for protected path: {}", path);
            return unauthorizedResponse(exchange, "Missing or invalid authorization token");
        }
        
        log.info("Token extracted successfully, validating for path: {}", path);
        
        // Validate token
        return jwtTokenProvider.validateAccessToken(token)
                .doOnNext(claims -> log.info("Token validated successfully for user: {} on path: {}", claims.get("sub"), path))
                .flatMap(claims -> {
                    // Add user info to request headers and delegate to downstream service
                    ServerWebExchange modifiedExchange = addUserHeaders(exchange, claims);
                    log.info("Proceeding to downstream service for path: {}", path);
                    return chain.filter(modifiedExchange);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // If the response is already committed downstream, avoid noisy logs and skip writing again
                    if (exchange.getResponse().isCommitted()) {
                        // Do not log after commit; benign empty due to downstream completion
                        return Mono.empty();
                    }
                    // Downgrade to WARN to avoid alarming logs for expected empty cases
                    log.warn("Token validation failed - empty result (likely expired or invalid type) for path: {}", path);
                    return unauthorizedResponse(exchange, "Invalid or expired token");
                }))
                .onErrorResume(ex -> {
                    if (exchange.getResponse().isCommitted()) {
                        // Do not log after the response has been committed by downstream
                        return Mono.empty();
                    }
                    // Only treat JWT-related problems as authentication failures
                    if (ex instanceof JwtException) {
                        // Downgrade to WARN; provide minimal token context
                        log.warn("Token validation failed for path {}: {} - Token: {}", path, ex.getMessage(),
                                token.length() > 10 ? token.substring(0, 10) + "..." : token);
                        return unauthorizedResponse(exchange, "Invalid or expired token: " + ex.getMessage());
                    }

                    // For non-JWT errors (e.g. downstream connection refused), propagate as 5xx
                    log.error("Downstream error after successful authentication for path {}: {}", path, ex.getMessage(), ex);
                    return Mono.error(ex);
                });
    }

    private boolean isPublicPath(String path) {
        String normalized = stripTrailingSlash(path);
        return PUBLIC_PATHS.stream().anyMatch(p -> {
            String pub = stripTrailingSlash(p);
            return normalized.equals(pub) || normalized.startsWith(pub + "/");
        });
    }

    private String normalizePath(String p) {
        if (p == null || p.isEmpty()) return "/";
        // Collapse multiple slashes to a single slash to avoid mismatches like "/api/merchant//verify-otp/mobile"
        String normalized = p.replaceAll("/{2,}", "/");
        // Ensure leading slash
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        // Remove trailing slash except for root
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String stripTrailingSlash(String s) {
        if (s == null || s.isEmpty() || "/".equals(s)) return "/";
        if (s.endsWith("/") && s.length() > 1) return s.substring(0, s.length() - 1);
        return s;
    }

    private ServerWebExchange addUserHeaders(ServerWebExchange exchange, Map<String, Object> claims) {
        return exchange.mutate()
                .request(builder -> {
                    // Resolve subject with fallbacks
                    String sub = claims.get("sub") != null ? String.valueOf(claims.get("sub")) : null;
                    String phoneNumber = claims.get("phoneNumber") != null ? String.valueOf(claims.get("phoneNumber")) : null;
                    String mobileNumber = claims.get("mobileNumber") != null ? String.valueOf(claims.get("mobileNumber")) : null;
                    String userIdClaim = claims.get("userId") != null ? String.valueOf(claims.get("userId")) : null;
                    String merchantIdClaim = claims.get("merchantId") != null ? String.valueOf(claims.get("merchantId")) : null;
                    // Backward compatibility: some tokens may carry registrationId as merchantId
                    if ((merchantIdClaim == null || merchantIdClaim.isBlank()) && claims.get("registrationId") != null) {
                        merchantIdClaim = String.valueOf(claims.get("registrationId"));
                    }
                    String resolvedUserId = (sub != null && !sub.isBlank()) ? sub
                            : (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber
                            : (mobileNumber != null && !mobileNumber.isBlank()) ? mobileNumber
                            : "";
                    if (userIdClaim != null && !userIdClaim.isBlank()) {
                        resolvedUserId = userIdClaim;
                    }

                    String usernameClaim = claims.get("username") != null ? String.valueOf(claims.get("username")) : null;
                    if ("null".equalsIgnoreCase(usernameClaim)) usernameClaim = null;
                    String emailClaim = claims.get("email") != null ? String.valueOf(claims.get("email")) : null;
                    if ("null".equalsIgnoreCase(emailClaim)) emailClaim = null;
                    String resolvedUsername = (usernameClaim != null && !usernameClaim.isBlank()) ? usernameClaim
                            : (emailClaim != null && !emailClaim.isBlank()) ? emailClaim
                            : (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber
                            : (mobileNumber != null && !mobileNumber.isBlank()) ? mobileNumber
                            : (resolvedUserId != null && !resolvedUserId.isBlank()) ? resolvedUserId
                            : null;

                    // Determine best role
                    String bestRole = null;
                    Object rolesClaim = claims.get("roles");
                    if (rolesClaim == null) rolesClaim = claims.get("authorities");
                    if (rolesClaim instanceof String rs) {
                        String up = rs.toUpperCase();
                        if (up.contains("SUPER_ADMIN")) bestRole = "SUPER_ADMIN";
                        else if (up.contains("ADMIN")) bestRole = "ADMIN";
                    } else if (rolesClaim instanceof java.util.Collection<?> col) {
                        for (Object r : col) {
                            if (r == null) continue;
                            String up = String.valueOf(r).toUpperCase();
                            if (up.contains("SUPER_ADMIN")) { bestRole = "SUPER_ADMIN"; break; }
                            if (up.equals("ADMIN") || up.contains("ADMIN")) bestRole = bestRole == null ? "ADMIN" : bestRole;
                        }
                    }
                    if (bestRole == null && claims.get("role") != null) {
                        bestRole = String.valueOf(claims.get("role"));
                    }

                    // Basic user info
                    builder.header("X-User-Id", resolvedUserId)
                           .header("X-Username", resolvedUsername != null ? resolvedUsername : "")
                           .header("X-User-Role", bestRole != null ? bestRole : String.valueOf(claims.get("role")))
                           .header("X-Session-Id", String.valueOf(claims.get("sessionId")));
                    if (merchantIdClaim != null && !merchantIdClaim.isBlank()) {
                        builder.header("X-Merchant-Id", merchantIdClaim);
                    }
                    
                    // Additional user info (only if present)
                    if (claims.get("email") != null) {
                        builder.header("X-User-Email", String.valueOf(claims.get("email")));
                    }
                    if (claims.get("fullName") != null) {
                        builder.header("X-User-FullName", String.valueOf(claims.get("fullName")));
                    }
                    // Admin specifics
                    if (claims.get("adminId") != null) {
                        builder.header("X-Admin-Id", String.valueOf(claims.get("adminId")));
                    }
                    if (phoneNumber != null && !phoneNumber.isBlank()) {
                        builder.header("X-User-Phone", phoneNumber);
                    } else if (mobileNumber != null && !mobileNumber.isBlank()) {
                        builder.header("X-User-Phone", mobileNumber);
                    }
                    
                    // Token metadata
                    if (claims.get("iat") != null) {
                        builder.header("X-Token-IssuedAt", String.valueOf(claims.get("iat")));
                    }
                    if (claims.get("exp") != null) {
                        builder.header("X-Token-ExpiresAt", String.valueOf(claims.get("exp")));
                    }
                    if (claims.get("jti") != null) {
                        builder.header("X-Token-Id", String.valueOf(claims.get("jti")));
                    }
                })
                .build();
    }

    private boolean hasAdminRole(Map<String, Object> claims) {
        Object roles = claims.get("roles");
        if (roles == null) roles = claims.get("authorities");
        if (roles == null) roles = claims.get("role");
        if (roles == null) return false;
        if (roles instanceof String s) {
            String normalized = s.toUpperCase();
            if (normalized.contains("SUPER_ADMIN") || normalized.contains("ADMIN")) return true;
            for (String part : normalized.split("[ ,]")) {
                if ("ADMIN".equals(part) || "SUPER_ADMIN".equals(part)) return true;
            }
            return false;
        }
        if (roles instanceof java.util.Collection<?> col) {
            for (Object r : col) {
                if (r != null) {
                    String ru = String.valueOf(r).toUpperCase();
                    if ("ADMIN".equals(ru) || "SUPER_ADMIN".equals(ru) || ru.contains("ADMIN")) return true;
                }
                if (r instanceof java.util.Map<?,?> m) {
                    Object v = m.get("authority");
                    if (v != null) {
                        String vu = String.valueOf(v).toUpperCase();
                        if (vu.contains("SUPER_ADMIN") || vu.contains("ADMIN")) return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        log.warn("Unauthorized access attempt: {}", message);
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            log.debug("Response already committed, cannot send unauthorized response");
            return Mono.empty();
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set("Content-Type", "application/json");
        String escapedMessage = message.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        String errorBody = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", escapedMessage);
        var buffer = response.bufferFactory().wrap(errorBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> forbiddenResponse(ServerWebExchange exchange, String message) {
        log.warn("Forbidden: {}", message);
        var response = exchange.getResponse();
        if (response.isCommitted()) {
            log.debug("Response already committed, cannot send forbidden response");
            return Mono.empty();
        }
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().set("Content-Type", "application/json");
        String escapedMessage = message.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        String errorBody = String.format("{\"error\":\"Forbidden\",\"message\":\"%s\"}", escapedMessage);
        var buffer = response.bufferFactory().wrap(errorBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Execute before other filters
    }
}
