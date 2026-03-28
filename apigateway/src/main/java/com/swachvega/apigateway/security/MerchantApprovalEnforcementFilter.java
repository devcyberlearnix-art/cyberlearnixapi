package com.swachvega.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("merchantApprovalEnforcementFilter")
public class MerchantApprovalEnforcementFilter implements GlobalFilter, Ordered {

    @Value("${security.jwt.access.secret:${jwt.access-token.secret}}")
    private String accessTokenSecret;

    // Allowed before admin approval (prefix match)
    private static final List<String> ALLOWED_PENDING_PREFIXES = List.of(
            "/api/merchant/store-timings",
            "/api/merchant/change-password",
            "/api/merchant/bank-details",
            "/api/merchant/address",
            "/api/merchant/contact-info/initiate",
            "/api/merchant/contact-info/resend/auth",
            "/api/merchant/contact-info/verify",
            "/api/merchant/complete-registration",
            "/api/merchant/upload-documents",
            "/api/merchant/set-password",
            "/api/merchant/resend-email-otp",
            "/api/merchant/verify-email-otp",
            "/api/merchant/resend-otp",
            "/api/merchant/signup",
            "/api/merchant/login",
            "/api/merchant/refresh-token",
            // Restaurant service: same onboarding paths allowed before approval
            "/api/restaurant/store-timings",
            "/api/restaurant/change-password",
            "/api/restaurant/bank-details",
            "/api/restaurant/address",
            "/api/restaurant/contact-info/initiate",
            "/api/restaurant/contact-info/resend/auth",
            "/api/restaurant/contact-info/verify",
            "/api/restaurant/complete-registration",
            "/api/restaurant/upload-documents",
            "/api/restaurant/set-password",
            "/api/restaurant/resend-email-otp",
            "/api/restaurant/verify-email-otp",
            "/api/restaurant/resend-otp",
            "/api/restaurant/signup",
            "/api/restaurant/login",
            "/api/restaurant/refresh-token"
    );

    private static boolean isAllowedForPending(String path) {
        if (path == null) return false;
        for (String prefix : ALLOWED_PENDING_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // Always allow CORS preflight
        if (method != null && HttpMethod.OPTIONS.equals(method)) {
            return chain.filter(exchange);
        }

        // If the path is explicitly allowed for pending merchants, let it pass
        if (isAllowedForPending(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // If no token, we cannot evaluate merchant approval; do not block other public flows
            return chain.filter(exchange);
        }

        try {
            String token = authHeader.substring(7);
            SecretKey key = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
            Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            Claims claims = jws.getPayload();

            // Determine if this token represents a merchant user
            String roles = extractRoles(claims);
            boolean roleSaysMerchant = roles != null && Arrays.stream(roles.toUpperCase().split(","))
                    .map(String::trim)
                    .anyMatch(r -> r.equals("MERCHANT"));
            // Some tokens may set role as ADMIN but still belong to merchant context; detect via merchant-specific claims
            boolean hasMerchantIdentifiers = claims.get("merchantId") != null || claims.get("registrationId") != null;
            boolean isMerchant = roleSaysMerchant || hasMerchantIdentifiers;
            if (!isMerchant) {
                return chain.filter(exchange);
            }

            // Read approval status from token if present
            String status = null;
            Object s1 = claims.get("status");
            if (s1 != null) status = String.valueOf(s1);
            if (status == null || status.isBlank()) {
                // Fallback to any reasonable alternative claim names
                Object s2 = claims.get("approvalStatus");
                if (s2 != null) status = String.valueOf(s2);
            }

            // If status is missing or not APPROVED, treat as not approved (defensive default)
            if (status == null || status.isBlank() || !status.equalsIgnoreCase("APPROVED")) {
                // Not approved: only allow whitelisted endpoints (already checked above)
                return forbidden(exchange, "Admin has not approved your account yet. Please wait until admin approves.");
            }

            // Approved or no status claim -> allow
            return chain.filter(exchange);
        } catch (Exception ex) {
            // On token parse errors, let other filters handle auth failures
            return chain.filter(exchange);
        }
    }

    private String extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles == null) roles = claims.get("authorities");
        if (roles == null) roles = claims.get("role");
        if (roles instanceof String s) return s;
        if (roles instanceof Collection<?> col) {
            return col.stream().map(String::valueOf).collect(Collectors.joining(","));
        }
        if (roles instanceof Map<?, ?> map && map.containsKey("roles")) {
            Object inner = map.get("roles");
            if (inner instanceof Collection<?> innerCol) {
                return innerCol.stream().map(String::valueOf).collect(Collectors.joining(","));
            }
            if (inner != null) return String.valueOf(inner);
        }
        return roles != null ? String.valueOf(roles) : null;
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"success\":false,\"message\":\"" + escapeJson(message) + "\",\"code\":\"ADMIN_APPROVAL_PENDING\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public int getOrder() {
        // Run after primary JWT auth filters but before admin enforcement
        return -85;
    }
}
