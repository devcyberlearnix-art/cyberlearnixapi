package com.example.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class AssignmentReminderRequest {

    private Long courseId;
    private UUID assignmentId;
    private UUID instructorId;

    private String title;
    private String message;

    private LocalDateTime dueDate;

    private Boolean sendToAll;
    private List<UUID> userIds;

    // dynamic channels
    private List<String> channels; // IN_APP, EMAIL, SMS
}
