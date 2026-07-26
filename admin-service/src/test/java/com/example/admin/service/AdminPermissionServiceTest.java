package com.example.admin.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AdminPermissionServiceTest {

    private final AdminPermissionService permissionService = new AdminPermissionService();

    @Test
    void allowCourseApprovalWithoutPrincipalForInternalAdminFlow() {
        assertDoesNotThrow(() -> permissionService.requireServiceAccess(null, "/admin/courses/1/approve"));
    }
}
