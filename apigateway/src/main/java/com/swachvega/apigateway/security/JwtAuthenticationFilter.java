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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("subcategoryJwtAuthFilter")
@ConditionalOnProperty(name = "gateway.subcategories.auth.enabled", havingValue = "true", matchIfMissing = false)
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<HttpMethod> WRITE_METHODS = List.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH);

    @Value("${jwt.access-token.secret}")
    private String accessTokenSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // Only enforce auth for write operations on subcategory APIs through gateway
        boolean isSubCategoryPath = path != null && path.startsWith("/api/subcategories/") || "/api/subcategories".equals(path);
        boolean isWriteMethod = method != null && WRITE_METHODS.contains(method);

        if (!(isSubCategoryPath && isWriteMethod)) {
            // For non-protected endpoints, still mark as via gateway
            return chain.filter(
                exchange.mutate().request(
                    exchange.getRequest().mutate()
                        .headers(h -> h.add("X-From-Gateway", "true"))
                        .build()
                ).build()
            );
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        try {
            SecretKey key = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
            Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            Claims claims = jws.getPayload();

            String userId = claims.getSubject();
            if (userId == null || userId.isBlank()) {
                // Try common alternative claim names
                Object uid = claims.get("userId");
                userId = uid != null ? String.valueOf(uid) : null;
            }
            if (userId == null || userId.isBlank()) {
                return unauthorized(exchange, "Token missing subject/userId");
            }

            // Extract roles claim if present (supports array or comma-separated string)
            String rolesHeader = extractRoles(claims);
            final String userIdHeader = userId;
            final String rolesHeaderFinal = rolesHeader;

            // Inject headers expected by downstream service
            return chain.filter(
                exchange.mutate().request(
                    exchange.getRequest().mutate()
                        .headers(h -> {
                            h.add("X-User-Id", userIdHeader);
                            if (rolesHeaderFinal != null && !rolesHeaderFinal.isBlank()) {
                                h.add("X-User-Roles", rolesHeaderFinal);
                            }
                            h.add("X-From-Gateway", "true");
                        })
                        .build()
                ).build()
            );
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid token: " + ex.getMessage());
        }
    }

    private String extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles == null) {
            roles = claims.get("authorities");
        }
        if (roles instanceof String s) {
            return s;
        }
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
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = ("{\"success\":false,\"message\":\"" + escapeJson(message) + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private String escapeJson(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public int getOrder() {
        // Run after the main JwtAuthenticationFilter (-100) to avoid conflicts
        return -90;
    }
}
