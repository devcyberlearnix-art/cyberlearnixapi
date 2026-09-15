package com.example.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Service to remember trusted devices verified by the user and suppress repetitive alerts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTrustedDeviceService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "trusted_device:";
    private static final Duration TRUSTED_DEVICE_TTL = Duration.ofDays(30);

    /**
     * Checks if the device signature has already been verified as trusted by the user.
     */
    public boolean isTrusted(UUID userId, String device, String operatingSystem) {
        if (userId == null) {
            return false;
        }

        try {
            String deviceHash = computeDeviceFingerprint(device, operatingSystem);
            String key = KEY_PREFIX + userId + ":" + deviceHash;
            Boolean isTrusted = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(isTrusted);
        } catch (Exception ex) {
            log.error("Redis lookup error for trusted device check userId={}: {}", userId, ex.getMessage());
            return false; // Fail-closed: treat as untrusted if lookup fails
        }
    }

    /**
     * Marks a device signature as trusted after user verification ("Yes, this was me").
     */
    public void markAsTrusted(UUID userId, String device, String operatingSystem) {
        if (userId == null) {
            return;
        }

        try {
            String deviceHash = computeDeviceFingerprint(device, operatingSystem);
            String key = KEY_PREFIX + userId + ":" + deviceHash;
            redisTemplate.opsForValue().set(key, "TRUSTED", TRUSTED_DEVICE_TTL);
            log.info("Marked device as trusted for userId={} [fingerprint={}]. Valid for 30 days.", userId, deviceHash);
        } catch (Exception ex) {
            log.error("Failed to mark device as trusted for userId={}: {}", userId, ex.getMessage(), ex);
        }
    }

    private String computeDeviceFingerprint(String device, String operatingSystem) {
        String raw = (device != null ? device.trim().toLowerCase() : "unknown") + "|" +
                     (operatingSystem != null ? operatingSystem.trim().toLowerCase() : "unknown");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return Integer.toHexString(raw.hashCode());
        }
    }
}
