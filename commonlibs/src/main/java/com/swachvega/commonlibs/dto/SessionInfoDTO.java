package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoDTO {
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
