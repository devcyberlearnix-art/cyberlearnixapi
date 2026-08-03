package com.example.notification.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequest {

    @NotEmpty
    private List<CreateNotificationRequest> notifications;
}
