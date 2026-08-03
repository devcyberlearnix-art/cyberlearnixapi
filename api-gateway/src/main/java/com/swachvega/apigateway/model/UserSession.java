package com.swachvega.apigateway.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class UserSession {
    // Getters and Setters
    private String sessionId;
    private String userId;
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private LocalDateTime expiresAt;
    private boolean active;

    public UserSession() {}

    public UserSession(String sessionId, String userId, String deviceId, String deviceName, 
                      String ipAddress, String userAgent, LocalDateTime expiresAt) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.active = true;
    }

}
