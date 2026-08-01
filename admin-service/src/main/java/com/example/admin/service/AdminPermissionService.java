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
        AssignedService required = resolveServiceFromPath(requestPath);
        if (required == null) {
            return;
        }
        if (principal.getAssignedService() == null || principal.getAssignedService() == AssignedService.ALL) {
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
        return path.startsWith("/admin/courses") || path.startsWith("/admin/content");
    }

    public AssignedService resolveServiceFromPath(String path) {
        if (path == null) return null;
        if (path.startsWith("/admin/orders")) return AssignedService.ORDER_SERVICE;
        if (path.startsWith("/admin/cart")) return AssignedService.CART_SERVICE;
        if (path.startsWith("/admin/coupon")) return AssignedService.COUPON_SERVICE;
        if (path.startsWith("/admin/payments")) return AssignedService.PAYMENT_SERVICE;
        if (path.startsWith("/admin/users")) return AssignedService.USER_SERVICE;
        if (path.startsWith("/admin/instructors")) return AssignedService.INSTRUCTOR_SERVICE;
        if (path.startsWith("/admin/courses") || path.startsWith("/admin/content")) return AssignedService.COURSE_SERVICE;
        if (path.startsWith("/admin/reports")) return null;
        if (path.startsWith("/admin/settings")) return null;
        if (path.startsWith("/admin/reviews")) return AssignedService.COURSE_SERVICE;
        if (path.equals("/admin/register")) return null;
        return null;
    }
}
