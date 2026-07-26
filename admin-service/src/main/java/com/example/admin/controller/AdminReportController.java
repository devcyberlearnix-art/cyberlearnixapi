package com.example.admin.controller;

import com.example.admin.dto.ApiResponse;
import com.example.admin.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "User statistics fetched successfully",
                        adminReportService.getUserReport(token),
                        now()
                )
        );
    }

    // ===== COURSES REPORT =====
    @GetMapping("/reports/courses")
    public ResponseEntity<ApiResponse<?>> getCoursesReport(
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Course statistics fetched successfully",
                        adminReportService.getCourseReport(token),
                        now()
                )
        );
    }

    // ===== REVENUE REPORT =====
    @GetMapping("/reports/revenue")
    public ResponseEntity<ApiResponse<?>> getRevenueReport(
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Revenue report fetched successfully",
                        adminReportService.getRevenueReport(token),
                        now()
                )
        );
    }

    // ===== ORDERS REPORT =====
    @GetMapping("/reports/orders")
    public ResponseEntity<ApiResponse<?>> getOrdersReport(
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Order analytics fetched successfully",
                        adminReportService.getOrderReport(token),
                        now()
                )
        );
    }

    // ===== SETTINGS =====
    @PutMapping("/settings/platform")
    public ResponseEntity<ApiResponse<?>> updatePlatform(@RequestBody Map<String, Object> data) {

        boolean result = adminReportService.updatePlatformSettings(data);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        result,
                        result ? "Platform settings updated successfully" : "Failed to update platform settings",
                        result,
                        now()
                )
        );
    }

    @PutMapping("/settings/payment")
    public ResponseEntity<ApiResponse<?>> updatePayment(@RequestBody Map<String, Object> data) {

        boolean result = adminReportService.updatePaymentSettings(data);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        result,
                        result ? "Payment settings updated successfully" : "Failed to update payment settings",
                        result,
                        now()
                )
        );
    }

    @PutMapping("/settings/notifications")
    public ResponseEntity<ApiResponse<?>> updateNotifications(@RequestBody Map<String, Object> data) {

        boolean result = adminReportService.updateNotificationSettings(data);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        result,
                        result ? "Notification settings updated successfully" : "Failed to update notification settings",
                        result,
                        now()
                )
        );
    }
}