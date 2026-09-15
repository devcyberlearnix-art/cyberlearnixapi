package com.user.register.security;

import com.cyberlearnix.security.ServiceAuthUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class ServiceAuthFilter extends OncePerRequestFilter {

    @Value("${service.auth.enabled:true}")
    private boolean serviceAuthEnabled;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private final ServiceAuthUtil serviceAuthUtil;

    public ServiceAuthFilter(ServiceAuthUtil serviceAuthUtil) {
        this.serviceAuthUtil = serviceAuthUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, java.io.IOException {
        
        // Prevent service auth from being disabled in production
        if (!serviceAuthEnabled) {
            if (activeProfile.contains("prod") || activeProfile.contains("production")) {
                log.error("CRITICAL: Service authentication is disabled in production profile!");
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Service authentication must be enabled in production");
                return;
            }
            log.warn("Service authentication disabled in non-production profile: {}", activeProfile);
            filterChain.doFilter(request, response);
            return;
        }

        // Check if this is an internal endpoint
        String path = request.getRequestURI();
        if (isInternalEndpoint(path)) {
            String authHeader = request.getHeader(serviceAuthUtil.getAuthHeaderName());
            
            if (authHeader == null || authHeader.isEmpty()) {
                log.warn("Missing service auth header for internal endpoint: {}", path);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing service authentication");
                return;
            }

            if (!serviceAuthUtil.validateServiceToken(authHeader)) {
                log.warn("Invalid service token for internal endpoint: {}", path);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid service authentication");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isInternalEndpoint(String path) {
        // Define which endpoints require service authentication
        return path.startsWith("/api/v1/instructors/") && path.contains("/profile");
    }
}