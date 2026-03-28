package com.swachvega.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionManagementResponseDTO {
    private boolean success;
    private String message;
    private List<SessionInfoDTO> sessions;
    private int activeSessionCount;
    private int maxAllowedSessions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfoDTO {
        private String sessionId;
        private String deviceId;
        private String deviceName;
        private String deviceType;
        private String ipAddress;
        private String location;
        private boolean isActive;
        private boolean isCurrent;
        private ZonedDateTime lastAccessedAt;
        private ZonedDateTime createdAt;
        private ZonedDateTime expiresAt;
        private String userAgent;
        private String browserName;
        private String osName;
    }
}
