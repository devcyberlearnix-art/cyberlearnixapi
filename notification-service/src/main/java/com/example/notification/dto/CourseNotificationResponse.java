package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CourseNotificationResponse {

    private UUID notificationId;
    private Long courseId;

    private String title;
    private String message;

    private String status;

    private List<String> channels;
    private List<String> targetUsers;

    private List<DeliveryInfo> deliveryStatuses;

    @Data
    @Builder
    public static class DeliveryInfo {
        private String userId;
        private String channel;
        private String status;
        private Instant attemptedAt;
    }
}