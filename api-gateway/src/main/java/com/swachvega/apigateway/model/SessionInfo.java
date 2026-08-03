package com.swachvega.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    private String sessionId;
    private String userId;
    private String username;
    private String email;
    private String role;
    private String deviceId;
    private String deviceName;
    private Instant createdAt;
    private Instant lastAccessedAt;
    private boolean active;
    
    // Additional session metadata
    private String ipAddress;
    private String userAgent;
    private String location;
}
