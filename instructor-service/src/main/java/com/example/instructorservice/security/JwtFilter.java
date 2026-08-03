package com.example.instructorservice.security;

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
import java.util.List;
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            String userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            request.setAttribute("userId", java.util.UUID.fromString(userId));
            request.setAttribute("role", role);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + toSpringSecurityRole(role)))
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            SecurityContextHolder.clearContext();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write(
                    "{\"error\": \"Invalid or expired token\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
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