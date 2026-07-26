package com.example.admin.security;

import com.example.admin.service.AdminPermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AdminAuthorizationFilter extends OncePerRequestFilter {

    private final AdminPermissionService adminPermissionService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/admin/")
                || path.equals("/admin/register")
                || path.equals("/admin/users")
                // Allow internal/service-crafted content endpoints without admin JWT
                || path.startsWith("/admin/sections")
                || path.matches("/admin/courses/\\d+/sections")
                // Allow admin API endpoints for orders, payments, and reviews
                || path.equals("/admin/orders")
                || path.equals("/admin/payments")
                || path.equals("/admin/reviews");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Skip authorization for admin API endpoints
        if (path.equals("/admin/orders") || path.equals("/admin/payments") || path.equals("/admin/reviews")) {
            filterChain.doFilter(request, response);
            return;
        }

        AdminPrincipal principal = AdminSecurityContext.getPrincipal();

        if ("/admin/register".equals(request.getRequestURI())
                && "POST".equalsIgnoreCase(request.getMethod())) {
            try {
                adminPermissionService.requireMainAdmin(principal);
            } catch (Exception e) {
                int status = e instanceof org.springframework.web.server.ResponseStatusException re
                        ? re.getStatusCode().value()
                        : HttpStatus.FORBIDDEN.value();
                response.setStatus(status);
                response.getWriter().write(e.getMessage() != null ? e.getMessage() : "Main Admin access required");
                return;
            }
        } else {
            try {
                adminPermissionService.requireServiceAccess(principal, request.getRequestURI());
            } catch (Exception e) {
                response.setStatus(e instanceof org.springframework.web.server.ResponseStatusException re
                        ? re.getStatusCode().value()
                        : HttpStatus.FORBIDDEN.value());
                response.getWriter().write(e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
