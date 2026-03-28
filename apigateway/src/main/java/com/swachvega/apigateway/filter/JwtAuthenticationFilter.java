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
            "/api/auth/",
            "/api/consumer/auth/",
            // Admin service public auth endpoints
            "/api/admin/register",
            "/api/admin/login",
            "/api/admin/refresh",
            "/api/admin/change-password",
            // Merchant public endpoints
            // Admin bootstrap: allow creating first admin without token
            "/api/merchant/admin/users",
            "/api/merchant/admin/user",
            // Admin auth endpoints should be public (no token required)
            "/api/merchant/admin/login",
            "/api/merchant/admin/refresh-token",
            "/api/merchant/signup",
            "/api/merchant/verify-otp",
            "/api/merchant/verify-otp/",
            "/api/merchant/verify-otp/mobile",
            "/api/merchant/verify-otp/email",
            "/api/merchant/verify-mobile-otp",
            "/api/merchant/verify-email-otp",
            "/api/merchant/resend-otp",
            "/api/merchant/resend-otp/",
            "/api/merchant/resend-otp/mobile",
            "/api/merchant/resend-otp/email",
            "/api/merchant/resend-email-otp",
            // New convenience upload endpoint that accepts merchantId without token
            "/api/merchant/upload-documents",
            // Staged signup follow-up endpoints (no tokens until admin approval)
            "/api/merchant/set-password",
            "/api/merchant/address",
            "/api/merchant/complete-registration",
            "/api/merchant/status",
            "/api/merchant/store-timings",
            "/api/merchant/bank-details",
            "/api/merchant/forgot-password/",
            "/api/merchant/login",
            "/api/merchant/refresh-token",
            // Global search endpoints (for testing/development)
            "/api/merchant/global-search",
            "/api/merchant/global-search/",
            "/api/merchant/global-search/autocomplete",
            "/api/merchant/global-search/suggestions",
            // Restaurant public endpoints (same onboarding flow as merchant)
            "/api/restaurant/signup",
            "/api/restaurant/verify-otp",
            "/api/restaurant/verify-mobile-otp",
            "/api/restaurant/verify-email-otp",
            "/api/restaurant/resend-otp",
            "/api/restaurant/resend-email-otp",
            "/api/restaurant/upload-documents",
            "/api/restaurant/set-password",
            "/api/restaurant/address",
            "/api/restaurant/complete-registration",
            "/api/restaurant/status",
            "/api/restaurant/store-timings",
            "/api/restaurant/bank-details",
            "/api/restaurant/forgot-password/",
            "/api/restaurant/login",
            "/api/restaurant/refresh-token",
            "/actuator/",
            "/health/",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/v3/api-docs/",
            "/userservice/swagger-ui/",
            "/userservice/v3/api-docs/",
            "/productservice/swagger-ui/",
            "/productservice/v3/api-docs/",
            "/orderservice/swagger-ui/",
            "/orderservice/v3/api-docs/",
            "/inventoryservice/swagger-ui/",
            "/inventoryservice/v3/api-docs/",
            "/searchservice/swagger-ui/",
            "/searchservice/v3/api-docs/",
            "/storeservice/swagger-ui/",
            "/storeservice/v3/api-docs/",
            "/landingpageservice/swagger-ui/",
            "/landingpageservice/v3/api-docs/",
            // PayU payment gateway callbacks – posted by PayU servers (no JWT present)
            "/api/payment/callback/success",
            "/api/payment/callback/failure",
            "/api/payment/webhook"
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
                    // If ADMIN or SUPER_ADMIN, allow all APIs across services
                    String role = String.valueOf(claims.getOrDefault("role", ""));
                    boolean isAdminAny = "ADMIN".equalsIgnoreCase(role)
                            || "SUPER_ADMIN".equalsIgnoreCase(role)
                            || hasAdminRole(claims);

                    if (isAdminAny) {
                        ServerWebExchange modifiedExchange = addUserHeaders(exchange, claims);
                        log.info("ADMIN/SUPER_ADMIN detected, allowing access to all APIs: {}", path);
                        return chain.filter(modifiedExchange);
                    }

                    // Otherwise apply merchant RBAC for non-admin roles
                    boolean isAdmin = false; // non-admin flow

                    // Coarse-grained RBAC rules
                    boolean isAdminOnly = path.startsWith("/api/merchant/admin/") || path.startsWith("/api/merchant/finance/");
                    boolean isStaffAllowed =
                            path.startsWith("/api/merchant/orders/") ||
                            path.startsWith("/api/merchant/inventory/") ||
                            path.startsWith("/api/merchant/products/lookup") ||
                            path.startsWith("/api/merchant/store-timings") ||
                            path.equals("/api/merchant/address") ||
                            path.startsWith("/api/merchant/address") ||
                            path.equals("/api/merchant/complete-registration") ||
                            path.startsWith("/api/merchant/complete-registration");

                    if (isAdminOnly && !isAdmin) {
                        log.warn("RBAC deny: ADMIN role required for path {} but role='{}'", path, role);
                        return forbiddenResponse(exchange, "Admin role required");
                    }
                    if (path.startsWith("/api/merchant/") && !isAdminOnly && !isStaffAllowed && !isAdmin) {
                        // Default any other merchant path to ADMIN-only unless explicitly staff-allowed
                        log.warn("RBAC deny: path {} not permitted for role='{}'", path, role);
                        return forbiddenResponse(exchange, "Insufficient role for this endpoint");
                    }

                    // Add user info to request headers for downstream services
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
