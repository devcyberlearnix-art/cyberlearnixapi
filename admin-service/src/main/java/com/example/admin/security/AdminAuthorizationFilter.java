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
        return !path.startsWith("/api/v1/admin/")
                || path.equals("/api/v1/admin/login")
                || path.equals("/api/v1/admin/login/otp/request")
                || path.equals("/api/v1/admin/login/otp/verify")
                || path.equals("/api/v1/admin/login/otp/resend")
                // Allow password recovery endpoints without admin JWT
                || path.equals("/api/v1/admin/password/forgot")
                || path.equals("/api/v1/admin/password/verify-otp")
                || path.equals("/api/v1/admin/password/reset")
                || path.equals("/api/v1/admin/password/otp/resend")
                // Allow internal/service-crafted content endpoints without admin JWT
                || path.startsWith("/api/v1/admin/sections")
                || path.matches("/api/v1/admin/courses/\\d+/sections");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        AdminPrincipal principal = AdminSecurityContext.getPrincipal();

        if ("/api/v1/admin/register".equals(request.getRequestURI())
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
