package com.cyberlearnix.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared JWT Authentication Filter for all microservices.
 * This filter validates JWT tokens and sets authentication in SecurityContext.
 * It supports both direct JWT validation and API Gateway header-based authentication.
 */
@Slf4j
public class SharedJwtAuthenticationFilter extends OncePerRequestFilter {

    private final SharedJwtValidator jwtValidator;

    public SharedJwtAuthenticationFilter(SharedJwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Check for API Gateway headers first (for microservice communication)
            String gatewayUserId = request.getHeader("X-User-Id");
            String gatewayRole = request.getHeader("X-User-Role");

            if (gatewayUserId != null && gatewayRole != null) {
                // Use API Gateway authentication
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + gatewayRole));
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        gatewayUserId,
                        null,
                        authorities
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Set authentication from API Gateway for user: {} with role: {}", gatewayUserId, gatewayRole);
            } else {
                // Fall back to JWT token validation
                String jwt = extractJwtFromRequest(request);

                if (StringUtils.hasText(jwt) && jwtValidator.isTokenValid(jwt)) {
                    String userId = jwtValidator.extractUserId(jwt);
                    String role = jwtValidator.extractRole(jwt);
                    String adminType = jwtValidator.extractAdminType(jwt);
                    String assignedService = jwtValidator.extractAssignedService(jwt);

                    // Build authorities
                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (role != null) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }

                    // Add admin type as authority if applicable
                    if (adminType != null && !adminType.equals("NONE")) {
                        authorities.add(new SimpleGrantedAuthority("ADMIN_TYPE_" + adminType));
                    }

                    // Add assigned service as authority if applicable
                    if (assignedService != null && !assignedService.equals("NONE")) {
                        authorities.add(new SimpleGrantedAuthority("SERVICE_" + assignedService));
                    }

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            authorities
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("Set authentication for user: {} with role: {}", userId, role);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header.
     * 
     * @param request HTTP request
     * @return JWT token or null
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
