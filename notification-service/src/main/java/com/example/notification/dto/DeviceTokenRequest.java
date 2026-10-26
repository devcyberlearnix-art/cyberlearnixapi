package com.example.notification.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {

    @NotBlank(message = "Device token is required")
    @JsonAlias({"deviceToken", "fcmToken", "pushToken"})
    private String token;

    private String deviceType = "WEB"; // ANDROID / IOS / WEB

    private String deviceId; // optional (for multiple devices)
}
