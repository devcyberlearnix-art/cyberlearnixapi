package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BulkNotificationResponse {

    private String status; // SUCCESS / PARTIAL_FAILURE
    private int totalRequested;
    private int successCount;
    private int failedCount;

    private List<NotificationResponse> successNotifications;
    private List<FailedNotification> failedNotifications;

    @Data
    @Builder
    public static class FailedNotification {
        private CreateNotificationRequest request;
        private String errorMessage;
    }
}
