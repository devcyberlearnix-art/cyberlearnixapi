package com.user.register.security;

import com.user.register.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnifiedJwtAuthenticationFilter extends OncePerRequestFilter {

    private final UnifiedJwtService unifiedJwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("=== UnifiedJwtAuthenticationFilter called for: {} ===", request.getRequestURI());

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

                if (StringUtils.hasText(jwt)) {
                    log.debug("JWT token found in request: {}", request.getRequestURI());
                    
                    // Check blacklist if service is available
                    boolean isBlacklisted = false;
                    if (tokenBlacklistService != null) {
                        isBlacklisted = tokenBlacklistService.isBlacklisted(jwt);
                    }
                    
                    log.debug("Token blacklisted: {}", isBlacklisted);
                    log.debug("UnifiedJwtService available: {}", unifiedJwtService != null);
                    
                    if (unifiedJwtService != null) {
                        boolean isValid = unifiedJwtService.validateToken(jwt);
                        boolean isExpired = unifiedJwtService.isTokenExpired(jwt);
                        log.debug("Token valid: {}, Token expired: {}", isValid, isExpired);
                    }
                    
                    if (!isBlacklisted && unifiedJwtService != null && unifiedJwtService.validateToken(jwt) && !unifiedJwtService.isTokenExpired(jwt)) {
                        String userId = unifiedJwtService.extractUserId(jwt);
                        String email = unifiedJwtService.extractEmail(jwt);
                        String role = unifiedJwtService.extractRole(jwt);
                        String adminType = unifiedJwtService.extractAdminType(jwt);
                        String assignedService = unifiedJwtService.extractAssignedService(jwt);

                        log.debug("Extracted from JWT - userId: {}, email: {}, role: {}", userId, email, role);

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

                        log.info("Set authentication for user: {} with role: {}", email, role);
                    } else {
                        log.warn("JWT validation failed or token blacklisted for request: {}", request.getRequestURI());
                    }
                } else {
                    log.debug("No JWT token found in request: {}", request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
