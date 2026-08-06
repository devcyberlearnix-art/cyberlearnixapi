package com.user.register.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user.register.dto.ApiResponse;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// @Component - Disabled to prevent conflict with UnifiedJwtAuthenticationFilter
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final TokenBlacklistService blacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return true;
        }
        // Skip filter for public auth endpoints, but NOT for:
        // - /switch-role (needs auth)
        // - /sessions (needs auth — list/logout sessions)
        if (path.equals("/api/v1/auth/register") ||
                path.equals("/api/v1/auth/verify-email") ||
                path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/login/password") ||
                path.startsWith("/api/v1/auth/login/otp/") ||
                path.equals("/api/v1/auth/refresh") ||
                path.startsWith("/api/v1/auth/password/") ||
                path.equals("/api/v1/auth/logout") ||
                path.equals("/api/v1/auth/upload/profile-photo") ||
                path.equals("/api/v1/users/login/social")) {
            return true;
        }
        return false;
    }

    private final org.springframework.security.web.context.SecurityContextRepository securityContextRepository = new org.springframework.security.web.context.RequestAttributeSecurityContextRepository();

    private void saveSecurityContext(HttpServletRequest request, HttpServletResponse response,
            UsernamePasswordAuthenticationToken auth) {
        org.springframework.security.core.context.SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("=== JWT FILTER doFilterInternal ===");
        System.out.println("Path: " + request.getRequestURI());

        // 🔥 Get data from API Gateway
        String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");

        if (userId != null && role != null) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            saveSecurityContext(request, response, auth);
        } else {
            String token = resolveToken(request, true);
            if (token != null && !token.isBlank()) {
                setAuthentication(token, request, response);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleInstructorBearerOnly(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            writeUnauthorized(response,
                    "Authorization header required. Format: Bearer <user_access_token>");
            return;
        }

        if (!authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            writeUnauthorized(response,
                    "Token must be sent as Bearer. Format: Authorization: Bearer <user_access_token>");
            return;
        }

        String token = authHeader.substring(7).trim();
        if (token.isBlank()) {
            writeUnauthorized(response, "Bearer token is empty");
            return;
        }

        try {
            jwtUtil.requireUserAccessToken(token);
            setAuthentication(token, request, response);
            filterChain.doFilter(request, response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            writeUnauthorized(response, e.getReason() != null ? e.getReason() : "Invalid Bearer token");
        } catch (Exception e) {
            writeUnauthorized(response, "Invalid or expired Bearer token");
        }
    }

    private void setAuthentication(String token, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (blacklistService.isBlacklisted(token)) {
                SecurityContextHolder.clearContext();
                return;
            }

            java.util.UUID userUuid = jwtUtil.resolveUserIdFromAccessToken(token, userRepository);
            String role = jwtUtil.extractRole(token);

            // Skip session check for ADMIN roles because admin sessions are not stored in
            // user-service
            boolean isAdmin = role != null && (role.toUpperCase().contains("MAIN_ADMIN") || role.toUpperCase().contains("SUB_ADMIN"));

            if (!isAdmin) {
                java.util.Optional<com.user.register.entity.UserSession> sessionOpt = userSessionRepository
                        .findByAccessToken(token);
                if (sessionOpt.isEmpty()) {
                    SecurityContextHolder.clearContext();
                    return;
                }
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userUuid.toString(),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + normalizeRole(role))));

            saveSecurityContext(request, response, auth);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request, boolean bearerOnly) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authHeader.substring(7).trim();
        }
        /*
         * if (bearerOnly) {
         * return null;
         * }
         * if (request.getCookies() != null) {
         * for (Cookie cookie : request.getCookies()) {
         * if ("accessToken".equals(cookie.getName())) {
         * return cookie.getValue();
         * }
         * }
         * }
         */
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("data", null);
        body.put("timestamp", LocalDateTime.now().toString());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private static String normalizeRole(String role) {
        if (role == null)
            return "STUDENT";
        String upper = role.toUpperCase();
        if (upper.contains("MAIN_ADMIN"))
            return "MAIN_ADMIN";
        if (upper.contains("SUB_ADMIN"))
            return "SUB_ADMIN";
        return "USER".equalsIgnoreCase(role) ? "STUDENT" : upper;
    }
}
