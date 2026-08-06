package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Detailed DTO representing an announcement as returned by the GET /course/{courseId} endpoint.
 * Fields correspond to the rich JSON example the user expects.
 */
@Data
@Builder
public class DetailedAnnouncementResponse {
    /**
     * Inner static DTO for per‑user/channel delivery information.
     */
    @Data
    @Builder
    public static class DeliveryInfo {
        private UUID userId;
        private String channel;
        private String status;
        private Instant attemptedAt;
    }

    private UUID announcementId;
    private String title;
    private String content;
    private Long courseId;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String priority; // e.g., HIGH, MEDIUM, LOW
    private String status;   // e.g., ACTIVE, INACTIVE
    private List<String> channels; // e.g., ["IN_APP", "EMAIL"]
    private List<String> targetUsers; // list of user IDs as strings
    private List<DeliveryInfo> deliveryStatuses; // detailed delivery info per user/channel
}
