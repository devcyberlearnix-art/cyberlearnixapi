package com.example.notification.dto;

import com.example.notification.enums.ChannelType;
import com.example.notification.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateNotificationRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotEmpty
    private List<UUID> userIds;

    @NotEmpty
    private List<ChannelType> channels;

    private Priority priority;
}