package com.example.notification.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class DeleteNotificationResponse {
    private String notificationId;
    private String operation;
    private String status;
    private String message;
    private LocalDateTime deletedAt;
    private String originalMessage;
}