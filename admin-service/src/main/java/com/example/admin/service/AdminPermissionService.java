package com.example.admin.service;

import com.example.admin.entity.AssignedService;
import com.example.admin.security.AdminPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminPermissionService {

    public void requireMainAdmin(AdminPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Main Admin Bearer token required. Login via POST /api/v1/auth/login then send "
                            + "Authorization: Bearer <accessToken>");
        }
        boolean isMainAdmin = "MAIN_ADMIN".equalsIgnoreCase(principal.getRole())
                || "MAIN_ADMIN".equalsIgnoreCase(principal.getAdminType());
        if (!isMainAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only Main Admin can perform this action. Sub Admins cannot register other admins.");
        }
    }

    public void requireServiceAccess(AdminPrincipal principal, String requestPath) {
        if (principal == null) {
            if (isInternalCourseApprovalPath(requestPath)) {
                return;
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        if ("MAIN_ADMIN".equalsIgnoreCase(principal.getRole())) {
            return;
        }
        // Check if user has admin role
        if (!"MAIN_ADMIN".equalsIgnoreCase(principal.getRole()) && !"SUB_ADMIN".equalsIgnoreCase(principal.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User does not have access to admin endpoints");
        }
        AssignedService required = resolveServiceFromPath(requestPath);
        if (required == null) {
            return;
        }
        if (principal.getAssignedService() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Sub Admin does not have an assigned service");
        }
        if (principal.getAssignedService() == AssignedService.ALL) {
            return;
        }
        if (principal.getAssignedService() != required) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Sub Admin does not have access to " + required.name());
        }
    }

    private boolean isInternalCourseApprovalPath(String path) {
        if (path == null) {
            return false;
        }
        return path.startsWith("/api/v1/admin/courses") || path.startsWith("/api/v1/admin/content");
    }

    public AssignedService resolveServiceFromPath(String path) {
        if (path == null) return null;
        if (path.startsWith("/api/v1/admin/orders")) return AssignedService.ORDER_SERVICE;
        if (path.startsWith("/api/v1/admin/cart")) return AssignedService.CART_SERVICE;
        if (path.startsWith("/api/v1/admin/coupon")) return AssignedService.COUPON_SERVICE;
        if (path.startsWith("/api/v1/admin/payments")) return AssignedService.PAYMENT_SERVICE;
        if (path.startsWith("/api/v1/admin/users")) return AssignedService.USER_SERVICE;
        if (path.startsWith("/api/v1/admin/instructors")) return AssignedService.INSTRUCTOR_SERVICE;
        if (path.startsWith("/api/v1/admin/courses") || path.startsWith("/api/v1/admin/content")) return AssignedService.COURSE_SERVICE;
        if (path.startsWith("/api/v1/admin/reports")) return null;
        if (path.startsWith("/api/v1/admin/settings")) return null;
        if (path.startsWith("/api/v1/admin/reviews")) return AssignedService.COURSE_SERVICE;
        if (path.equals("/api/v1/admin/register")) return null;
        return null;
    }
}
