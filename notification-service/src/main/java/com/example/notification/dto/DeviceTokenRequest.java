package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {

    @NotBlank(message = "Device token is required")
    private String token;

    @NotBlank(message = "Device type is required") // ANDROID / IOS / WEB
    private String deviceType;

    private String deviceId; // optional (for multiple devices)
}
