package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationRetryResponse {

    private String notificationId;
    private String operation;
    private String status;
    private String message;
    private LocalDateTime retriedAt;

    private RetryDetails retryDetails;

    @Data
    @Builder
    public static class RetryDetails {
        private int userCount;
        private int channelCount;
        private int totalRetryAttempts;
        private String retryReason;
    }
}