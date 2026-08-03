package com.lms.coupon_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(jwt)) {
                String userId = jwtService.extractUserId(jwt);
                String role = jwtService.extractRole(jwt);
                String assignedService = jwtService.extractAssignedService(jwt);

                logger.debug("JWT Extracted - userId: {}, role: {}, assignedService: {}", userId, role, assignedService);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Build authorities list
                    List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                    
                    // Add role authority (ROLE_SUB_ADMIN)
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    
                    // Add service authority if assignedService is present (SERVICE_COUPON_SERVICE)
                    if (assignedService != null && !assignedService.isBlank() && !"NONE".equals(assignedService)) {
                        authorities.add(new SimpleGrantedAuthority("SERVICE_" + assignedService));
                    }
                    
                    // Add ALL service authority for MAIN_ADMIN
                    if ("MAIN_ADMIN".equals(role)) {
                        authorities.add(new SimpleGrantedAuthority("SERVICE_ALL"));
                    }

                    logger.debug("JWT Filter - Role: {}, AssignedService: {}, Authorities: {}", role, assignedService, authorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("JWT Validation failed in Coupon Service: {}", e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid or expired token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}