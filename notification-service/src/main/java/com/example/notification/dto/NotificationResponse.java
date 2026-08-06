package com.example.notification.dto;

import com.example.notification.enums.ChannelType;
import com.example.notification.enums.DeliveryStatus;
import com.example.notification.enums.NotificationStatus;
import com.example.notification.enums.Priority;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
public class NotificationResponse {

    private String notificationId;
    private String title;
    private String message;

    private List<UUID> userIds;
    private List<ChannelType> channels;

    private NotificationStatus status;
    private Priority priority;

    private LocalDateTime createdAt;

    private List<DeliveryInfo> deliveryStatuses;

    @Data
    @Builder
    public static class DeliveryInfo {

        private UUID userId;
        private ChannelType channel;
        private DeliveryStatus status;
        private String error;
        private LocalDateTime attemptedAt;
    }
}