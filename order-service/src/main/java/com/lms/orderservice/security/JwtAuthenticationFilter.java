package com.lms.orderservice.security;



import jakarta.servlet.FilterChain;

import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;



import java.io.IOException;

import java.util.Collections;

import java.util.List;



import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter  {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // No Bearer token -> continue.
        // SecurityConfig will decide whether the endpoint requires authentication.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {

            if (!jwtService.isTokenValid(jwt)) {
                writeUnauthorized(request, response);
                return;
            }

            String userId = jwtService.extractUserId(jwt);
            String role = jwtService.extractRole(jwt);

            if (userId != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                List<SimpleGrantedAuthority> authorities =
                        Collections.singletonList(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + toSpringSecurityRole(role)));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {

            log.error("JWT authentication failed", e);

                writeUnauthorized(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isBlank()) {
            traceId = java.util.UUID.randomUUID().toString();
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setHeader("X-Trace-Id", traceId);
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":401,\"code\":\"UNAUTHORIZED\",\"message\":\"Invalid or expired token\",\"path\":\"%s\",\"traceId\":\"%s\"}",
                java.time.Instant.now(), request.getRequestURI(), traceId));
    }

    private static String toSpringSecurityRole(String role) {

        if (role == null || role.isBlank()) {
            return "STUDENT";
        }

        String upper = role.toUpperCase();

        if ("USER".equals(upper) || "STUDENT".equals(upper)) {
            return "STUDENT";
        }

        if (upper.contains("MAIN_ADMIN")) {
            return "MAIN_ADMIN";
        }

        if (upper.contains("SUB_ADMIN")) {
            return "SUB_ADMIN";
        }

        return upper;
    }

}