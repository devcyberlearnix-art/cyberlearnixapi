package com.example.admin.controller;

import com.example.admin.dto.ApiResponse;
import com.example.admin.security.AdminPrincipal;
import com.example.admin.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    private String now() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ===== USERS REPORT =====
    @GetMapping("/reports/users")
    public ResponseEntity<ApiResponse<?>> getUsersReport(
            @AuthenticationPrincipal AdminPrincipal adminPrincipal,
            @RequestHeader("Authorization") String authorization) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User statistics fetched successfully",
                        adminReportService.getUserReport(authorization),
                        now()
                )
        );
    }

    // ===== COURSES REPORT =====
    @GetMapping("/reports/courses")
    public ResponseEntity<ApiResponse<?>> getCoursesReport(
            @AuthenticationPrincipal AdminPrincipal adminPrincipal,
            @RequestHeader("Authorization") String authorization) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Course statistics fetched successfully",
                        adminReportService.getCourseReport(authorization),
                        now()
                )
        );
    }

    // ===== REVENUE REPORT =====
    @GetMapping("/reports/revenue")
    public ResponseEntity<ApiResponse<?>> getRevenueReport(
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Revenue report fetched successfully",
                        adminReportService.getRevenueReport(null), // Service will use service-to-service auth
                        now()
                )
        );
    }

    // ===== ORDERS REPORT =====
    @GetMapping("/reports/orders")
    public ResponseEntity<ApiResponse<?>> getOrdersReport(
            @AuthenticationPrincipal AdminPrincipal adminPrincipal) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Order analytics fetched successfully",
                        adminReportService.getOrderReport(null), // Service will use service-to-service auth
                        now()
                )
        );
    }

    // ===== SETTINGS =====
    @PutMapping("/settings/platform")
    public ResponseEntity<ApiResponse<?>> updatePlatform(@RequestBody Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Platform settings payload cannot be empty");
        }

        boolean result = adminReportService.updatePlatformSettings(data);
        if (!result) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Failed to update platform settings");
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Platform settings updated successfully",
                        true,
                        now()
                )
        );
    }

    @PutMapping("/settings/payment")
    public ResponseEntity<ApiResponse<?>> updatePayment(@RequestBody Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Payment settings payload cannot be empty");
        }

        boolean result = adminReportService.updatePaymentSettings(data);
        if (!result) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Failed to update payment settings");
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Payment settings updated successfully",
                        true,
                        now()
                )
        );
    }

    @PutMapping("/settings/notifications")
    public ResponseEntity<ApiResponse<?>> updateNotifications(@RequestBody Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Notification settings payload cannot be empty");
        }

        boolean result = adminReportService.updateNotificationSettings(data);
        if (!result) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Failed to update notification settings");
        }

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Notification settings updated successfully",
                        true,
                        now()
                )
        );
    }
}
