package com.example.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeviceTokenResponse {

    private String deviceTokenId;
    private String userId;
    private String token;
    private String deviceType;
    private String deviceId;
    private boolean active;
    private LocalDateTime createdAt;
}
