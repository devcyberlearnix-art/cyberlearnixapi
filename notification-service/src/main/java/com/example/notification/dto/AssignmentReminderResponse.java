package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AssignmentReminderResponse {

    private UUID reminderId;
    private Long courseId;
    private UUID assignmentId;
    private UUID instructorId;

    private String title;
    private String message;

    private Instant dueAt;
    private String status; // CREATED, SCHEDULED, SENT

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