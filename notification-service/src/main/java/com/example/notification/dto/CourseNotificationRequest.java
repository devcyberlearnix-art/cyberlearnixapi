package com.example.notification.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CourseNotificationRequest {

    private String title;
    private String message;

    private Boolean sendToAll;
    private List<UUID> userIds;

    private List<String> channels;
}