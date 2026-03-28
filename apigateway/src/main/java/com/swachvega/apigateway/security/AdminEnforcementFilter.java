package com.swachvega.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component("adminEnforcementFilter")
@ConditionalOnProperty(name = "gateway.admin.auth.enabled", havingValue = "true", matchIfMissing = true)
public class AdminEnforcementFilter implements GlobalFilter, Ordered {

    @Value("${jwt.access-token.secret}")
    private String accessTokenSecret;

    // Admin-only route patterns (prefix match)
    @Value("${gateway.admin.protected-prefixes:/api/admin/}")
    private List<String> protectedPrefixes;

    // Public admin auth endpoints (skip enforcement)
    private static final List<String> PUBLIC_ADMIN_PATHS = List.of(
            "/api/admin/register",
            "/api/admin/login",
            "/api/admin/refresh",
            "/api/admin/change-password"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!isProtected(path) || isPublicAdminPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
            Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            Claims claims = jws.getPayload();
            // Accept ADMIN or SUPER_ADMIN from common claim shapes
            String rolesStr = extractRolesAsString(claims);
            String normalized = rolesStr == null ? "" : rolesStr.toUpperCase();
            boolean hasAdmin = normalized.contains("ADMIN");
            boolean hasSuperAdmin = normalized.contains("SUPER_ADMIN");
            if (!(hasAdmin || hasSuperAdmin)) {
                return unauthorized(exchange, "ADMIN or SUPER_ADMIN role required");
            }
            // allow
            return chain.filter(exchange);
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid token: " + ex.getMessage());
        }
    }

    private boolean isProtected(String path) {
        if (path == null) return false;
        List<String> prefixes = (protectedPrefixes == null || protectedPrefixes.isEmpty())
                ? List.of("/api/admin/") : protectedPrefixes;
        for (String prefix : prefixes) {
            if (prefix != null && !prefix.isBlank() && path.startsWith(prefix.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPublicAdminPath(String path) {
        if (path == null) return false;
        for (String pub : PUBLIC_ADMIN_PATHS) {
            if (path.equals(pub)) return true;
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private String extractRolesAsString(Claims claims) {
        Object roles = claims.get("roles");
        if (roles == null) roles = claims.get("authorities");
        if (roles == null) roles = claims.get("role");
        if (roles == null) return null;
        if (roles instanceof String s) return s;
        if (roles instanceof java.util.Collection<?> col) {
            return col.stream().map(String::valueOf).reduce((a,b)->a+","+b).orElse("");
        }
        if (roles instanceof java.util.Map<?,?> m) {
            Object inner = m.get("roles");
            if (inner instanceof java.util.Collection<?> ic) {
                return ic.stream().map(String::valueOf).reduce((a,b)->a+","+b).orElse("");
            }
            if (inner != null) return String.valueOf(inner);
        }
        return String.valueOf(roles);
    }

    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public int getOrder() {
        // Run later than primary auth filters
        return -80;
    }
}
