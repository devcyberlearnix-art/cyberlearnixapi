package com.example.notification.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AnnouncementResponse {

    private UUID announcementId;
    private String title;
    private String message;
    private Boolean sendToAll;
    private List<UUID> userIds;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private String status;
    private Long courseId;
    private UUID createdBy;

}