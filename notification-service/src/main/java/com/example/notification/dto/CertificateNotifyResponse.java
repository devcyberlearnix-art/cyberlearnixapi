package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CertificateNotifyResponse {

    private UUID notificationId;
    private Long courseId;
    private UUID certificateId;
    private UUID instructorId;

    private String title;
    private String message;

    private Instant issuedAt;
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