package com.example.notification.service;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Service to manage time-limited, signed security tokens and track user verification actions.
 */
@Slf4j
@Service
public class SecurityVerificationTrackingService {

    private final StringRedisTemplate redisTemplate;
    private final String securitySecret;
    private final String securityBaseUrl;

    private static final String TRACKING_KEY_PREFIX = "security:action_audit:";
    private static final long DEFAULT_TOKEN_VALIDITY_SECONDS = 900; // 15 minutes

    public SecurityVerificationTrackingService(
            StringRedisTemplate redisTemplate,
            @Value("${jwt.access-token.secret:8c4e9d2f1a7b6c5d9e3f0a1b7c8d4e5f9a2b6c1d8e7f3a4b5c9d1e6f8a2b7c3}") String securitySecret,
            @Value("${app.security.base-url:https://cyberlearnix.com}") String securityBaseUrl) {
        this.redisTemplate = redisTemplate;
        this.securitySecret = securitySecret;
        this.securityBaseUrl = securityBaseUrl.endsWith("/") ? securityBaseUrl.substring(0, securityBaseUrl.length() - 1) : securityBaseUrl;
    }

    @Getter
    @Builder
    public static class VerificationTokenPayload {
        private UUID eventId;
        private UUID userId;
        private long expirationEpochSec;
        private boolean isValid;
        private boolean isExpired;
        private String errorMessage;
    }

    /**
     * Generates a time-limited, cryptographically signed HTTPS verification URL.
     * Default validity: 15 minutes.
     */
    public String generateSignedVerificationUrl(UUID eventId, UUID userId) {
        long expirationEpochSec = Instant.now().getEpochSecond() + DEFAULT_TOKEN_VALIDITY_SECONDS;
        String rawPayload = eventId + ":" + userId + ":" + expirationEpochSec;
        String signature = sign(rawPayload, securitySecret);

        String combined = rawPayload + ":" + signature;
        String base64Token = Base64.getUrlEncoder().withoutPadding().encodeToString(combined.getBytes(StandardCharsets.UTF_8));

        return String.format("%s/api/v1/security/verify-activity?token=%s", securityBaseUrl, base64Token);
    }

    /**
     * Validates a token: verifies signature and checks expiration.
     */
    public VerificationTokenPayload validateToken(String token) {
        if (token == null || token.isBlank()) {
            return VerificationTokenPayload.builder()
                    .isValid(false)
                    .errorMessage("Verification token is missing.")
                    .build();
        }

        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(token.trim());
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = decodedString.split(":");

            if (parts.length != 4) {
                return VerificationTokenPayload.builder()
                        .isValid(false)
                        .errorMessage("Malformed verification token.")
                        .build();
            }

            UUID eventId = UUID.fromString(parts[0]);
            UUID userId = UUID.fromString(parts[1]);
            long expEpochSec = Long.parseLong(parts[2]);
            String incomingSignature = parts[3];

            // Reconstruct payload and verify signature
            String rawPayload = eventId + ":" + userId + ":" + expEpochSec;
            String expectedSignature = sign(rawPayload, securitySecret);

            if (!expectedSignature.equals(incomingSignature)) {
                log.warn("Invalid signature for verification token userId={}, eventId={}", userId, eventId);
                return VerificationTokenPayload.builder()
                        .isValid(false)
                        .errorMessage("Invalid or tampered verification token signature.")
                        .build();
            }

            // Check expiration
            long currentEpochSec = Instant.now().getEpochSecond();
            if (currentEpochSec > expEpochSec) {
                log.warn("Expired verification token used. exp={}, current={}", expEpochSec, currentEpochSec);
                return VerificationTokenPayload.builder()
                        .eventId(eventId)
                        .userId(userId)
                        .expirationEpochSec(expEpochSec)
                        .isValid(false)
                        .isExpired(true)
                        .errorMessage("This security link has expired. For your safety, links are only valid for 15 minutes.")
                        .build();
            }

            return VerificationTokenPayload.builder()
                    .eventId(eventId)
                    .userId(userId)
                    .expirationEpochSec(expEpochSec)
                    .isValid(true)
                    .isExpired(false)
                    .build();

        } catch (Exception ex) {
            log.error("Error parsing verification token: {}", ex.getMessage());
            return VerificationTokenPayload.builder()
                    .isValid(false)
                    .errorMessage("Unable to validate security token: " + ex.getMessage())
                    .build();
        }
    }

    /**
     * Records and tracks user verification actions in the audit log and Redis.
     */
    public void trackAction(UUID eventId, UUID userId, String actionType, String clientIp, String userAgent) {
        String logEntry = String.format("action=%s, eventId=%s, userId=%s, ip=%s, time=%s",
                actionType, eventId, userId, clientIp, Instant.now());
        log.info("🛡️ [SECURITY ACTION TRACKED] {}", logEntry);

        try {
            String key = TRACKING_KEY_PREFIX + userId + ":" + eventId;
            redisTemplate.opsForValue().set(key, logEntry, Duration.ofDays(7));
        } catch (Exception ex) {
            log.error("Failed to persist security action audit to Redis: {}", ex.getMessage());
        }
    }

    private String sign(String data, String secret) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hmacBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("HMAC signing failure: " + e.getMessage(), e);
        }
    }
}
