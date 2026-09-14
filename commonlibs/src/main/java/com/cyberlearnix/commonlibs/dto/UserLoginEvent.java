package com.cyberlearnix.commonlibs.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event emitted after each successful user login.
 */
public record UserLoginEvent(
        UUID eventId,
        String eventType,
        UUID userId,
        String email,
        String username,
        LocalDateTime loginTime,
        String ipAddress,
        String device,
        String browser,
        String operatingSystem,
        String location,
        boolean isNewDevice
) {}
