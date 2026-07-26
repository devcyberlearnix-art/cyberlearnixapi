package com.example.notification.controller;

import com.example.notification.dto.ApiResponse;
import com.example.notification.dto.UserNotificationResponse;
import com.example.notification.service.UserNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    public UserNotificationController(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @GetMapping("/me/notifications")
    public ResponseEntity<ApiResponse<List<UserNotificationResponse>>> getMyNotifications() {

        List<UserNotificationResponse> notifications =
                userNotificationService.getMyNotifications();

        String message = notifications.isEmpty()
                ? "No notifications found"
                : "Notifications fetched successfully";

        return ResponseEntity.ok(
                ApiResponse.success(message, notifications)
        );
    }
    @GetMapping("/me/unread")
    public ResponseEntity<ApiResponse<List<UserNotificationResponse>>> getUnreadNotifications() {

        List<UserNotificationResponse> response =
                userNotificationService.getUnreadNotifications();

        String message = response.isEmpty()
                ? "No unread notifications"
                : "Unread notifications fetched";

        return ResponseEntity.ok(
                ApiResponse.success(message, response)
        );
    }
    @PutMapping("/me/read/{id}")
    public ResponseEntity<ApiResponse<UserNotificationResponse>> markAsRead(
            @PathVariable String id) {

        UserNotificationResponse response =
                userNotificationService.markAsRead(id);

        return ResponseEntity.ok(ApiResponse.success("success", response));
    }
    @PutMapping("/me/read-all")
    public ResponseEntity<ApiResponse<List<UserNotificationResponse>>> markAllAsRead() {

        List<UserNotificationResponse> response =
                userNotificationService.markAllAsRead();

        return ResponseEntity.ok(ApiResponse.success("Notifications fetched successfully", response));
    }
    @GetMapping("/me/count")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUnreadCount() {

        int count = userNotificationService.getUnreadCount();

        Map<String, Integer> data = Map.of("unreadCount", count);

        return ResponseEntity.ok(
                ApiResponse.success("Unread count fetched successfully", data)
        );

    }
}