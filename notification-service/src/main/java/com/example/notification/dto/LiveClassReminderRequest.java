package com.example.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class LiveClassReminderRequest {

    private Long courseId;
    private UUID instructorId;

    private String title;
    private String message;

    private LocalDateTime classStartTime;

    private Boolean sendToAll;
    private List<UUID> userIds;

    // ✅ dynamic channels from request
    private List<String> channels; // IN_APP, EMAIL, SMS
}
