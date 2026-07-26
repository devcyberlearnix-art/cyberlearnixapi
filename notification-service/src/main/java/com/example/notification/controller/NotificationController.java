package com.example.notification.controller;

import com.example.notification.dto.*;
import com.example.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @RequestBody CreateNotificationRequest request) {

        NotificationResponse response = notificationService.createNotification(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Notification created successfully",
                        response
                ));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkNotificationResponse>> createBulkNotifications(
            @RequestBody @Valid BulkNotificationRequest request) {

        BulkNotificationResponse response = notificationService.createBulkNotifications(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Bulk notifications processed successfully",
                        response
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            @PathVariable String id) {

        NotificationResponse response = notificationService.getNotificationById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification fetched successfully",
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications() {

        List<NotificationResponse> response = notificationService.getAllNotifications();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All notifications fetched successfully",
                        response
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByUserId(
            @PathVariable String userId) {

        List<NotificationResponse> response =
                notificationService.getNotificationsByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notifications fetched successfully for user: " + userId,
                        response
                )
        );
    }

    // Returns both allNotifications and unreadNotifications for a user in one call
    @GetMapping("/user/{userId}/combined")
    public ResponseEntity<ApiResponse<CombinedUserNotificationsResponse>> getCombinedUserNotifications(
            @PathVariable String userId) {

        List<NotificationResponse> all = notificationService.getNotificationsByUserId(userId);
        List<NotificationResponse> unread = notificationService.getUnreadNotificationsByUserId(userId);

        CombinedUserNotificationsResponse combined = CombinedUserNotificationsResponse.builder()
                .allNotifications(all)
                .unreadNotifications(unread)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Combined notifications fetched successfully for user: " + userId, combined)
        );
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable String id) {

        NotificationResponse response = notificationService.markNotificationAsRead(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification marked as READ successfully",
                        response
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteNotificationResponse> deleteNotification(@PathVariable String id) {

        DeleteNotificationResponse response = notificationService.deleteNotification(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<NotificationRetryResponse> retryNotification(@PathVariable String id) {

        NotificationRetryResponse response = notificationService.retryNotification(id);
        return ResponseEntity.ok(response);
    }
}