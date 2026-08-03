package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserNotificationResponse {

    private UUID notificationId;
    private String title;
    private String message;
    private String priority;
    private String status;
    private LocalDateTime createdAt;

    private boolean read;
    private List<String> channels;
}