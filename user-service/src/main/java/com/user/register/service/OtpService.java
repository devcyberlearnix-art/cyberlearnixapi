package com.user.register.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private static final String OTP_SESSION_PREFIX = "OTP:SESSION:";
    private static final String OTP_COOLDOWN_PREFIX = "OTP:COOLDOWN:";
    private static final String OTP_LATEST_PREFIX = "OTP:LATEST:";

    public record OtpSession(String sessionId, LocalDateTime expiresAt) {}

    public record OtpVerifyResult(boolean valid, String reason, int remainingAttempts) {}

    public String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    public OtpSession createSession(String email, String otpType, String otp, int validMinutes, int maxAttempts) {
        String sessionId = UUID.randomUUID().toString();
        String key = OTP_SESSION_PREFIX + sessionId;
        String latestKey = OTP_LATEST_PREFIX + otpType + ":" + email;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(validMinutes);

        redisTemplate.opsForHash().put(key, "email", email);
        redisTemplate.opsForHash().put(key, "otpType", otpType);
        redisTemplate.opsForHash().put(key, "otpHash", sha256(otp));
        redisTemplate.opsForHash().put(key, "remainingAttempts", String.valueOf(maxAttempts));
        redisTemplate.opsForHash().put(key, "verified", "false");
        redisTemplate.opsForHash().put(key, "expiresAt", expiresAt.toString());
        redisTemplate.expire(key, Duration.ofMinutes(validMinutes));
        redisTemplate.opsForValue().set(latestKey, sessionId, Duration.ofMinutes(validMinutes));

        return new OtpSession(sessionId, expiresAt);
    }

    public String getLatestSessionId(String email, String otpType) {
        return redisTemplate.opsForValue().get(OTP_LATEST_PREFIX + otpType + ":" + email);
    }

    public long getLatestSessionTtlSeconds(String email, String otpType) {
        String sessionId = getLatestSessionId(email, otpType);
        if (sessionId == null || sessionId.isBlank()) {
            return 0;
        }
        return getSessionTtlSeconds(sessionId);
    }

    public long getSessionTtlSeconds(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return 0;
        }
        Long ttl = redisTemplate.getExpire(OTP_SESSION_PREFIX + sessionId);
        if (ttl == null || ttl < 0) {
            return 0;
        }
        return ttl;
    }

    public Optional<LocalDateTime> getSessionExpiresAt(String sessionId) {
        long ttl = getSessionTtlSeconds(sessionId);
        if (ttl <= 0) {
            return Optional.empty();
        }
        return Optional.of(LocalDateTime.now().plusSeconds(ttl));
    }

    public OtpVerifyResult verifyLatestSession(String email, String otp, String otpType, boolean consumeOnSuccess) {
        String sessionId = getLatestSessionId(email, otpType);
        if (sessionId == null || sessionId.isBlank()) {
            return new OtpVerifyResult(false, "OTP session expired or not found", 0);
        }
        return verifySession(sessionId, email, otp, otpType, consumeOnSuccess);
    }

    public long getCooldownSeconds(String email, String otpType) {
        String cooldownKey = OTP_COOLDOWN_PREFIX + otpType + ":" + email;
        Long ttl = redisTemplate.getExpire(cooldownKey);
        if (ttl == null || ttl < 0) {
            return 0;
        }
        return ttl;
    }

    public void markCooldown(String email, String otpType, int seconds) {
        String cooldownKey = OTP_COOLDOWN_PREFIX + otpType + ":" + email;
        redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(seconds));
    }

    public OtpVerifyResult verifySession(String sessionId, String email, String otp, String otpType, boolean consumeOnSuccess) {
        String key = OTP_SESSION_PREFIX + sessionId;

        Object storedEmail = redisTemplate.opsForHash().get(key, "email");
        Object storedType = redisTemplate.opsForHash().get(key, "otpType");
        Object storedHash = redisTemplate.opsForHash().get(key, "otpHash");
        Object storedRemaining = redisTemplate.opsForHash().get(key, "remainingAttempts");
        Object storedVerified = redisTemplate.opsForHash().get(key, "verified");

        if (storedEmail == null || storedType == null || storedHash == null || storedRemaining == null) {
            return new OtpVerifyResult(false, "OTP session expired or not found", 0);
        }

        if (!email.equals(storedEmail.toString()) || !otpType.equals(storedType.toString())) {
            return new OtpVerifyResult(false, "OTP session does not match email/type", 0);
        }

        if (Boolean.parseBoolean(String.valueOf(storedVerified))) {
            return new OtpVerifyResult(false, "OTP already used", 0);
        }

        int remainingAttempts = Integer.parseInt(String.valueOf(storedRemaining));
        if (remainingAttempts <= 0) {
            return new OtpVerifyResult(false, "OTP attempts exhausted", 0);
        }

        boolean isValid = sha256(otp).equals(storedHash.toString());
        if (!isValid) {
            int updatedRemaining = remainingAttempts - 1;
            redisTemplate.opsForHash().put(key, "remainingAttempts", String.valueOf(updatedRemaining));
            return new OtpVerifyResult(false, "Invalid OTP", Math.max(updatedRemaining, 0));
        }

        redisTemplate.opsForHash().put(key, "verified", "true");
        if (consumeOnSuccess) {
            redisTemplate.delete(key);
        }

        return new OtpVerifyResult(true, "OTP verified", remainingAttempts);
    }

    public Optional<String> resolveSessionEmail(String sessionId, String otpType) {
        String key = OTP_SESSION_PREFIX + sessionId;
        Object storedEmail = redisTemplate.opsForHash().get(key, "email");
        Object storedType = redisTemplate.opsForHash().get(key, "otpType");
        Object storedVerified = redisTemplate.opsForHash().get(key, "verified");

        if (storedEmail == null || storedType == null) {
            return Optional.empty();
        }

        if (!otpType.equals(storedType.toString())) {
            return Optional.empty();
        }

        if (Boolean.parseBoolean(String.valueOf(storedVerified))) {
            return Optional.empty();
        }

        return Optional.of(storedEmail.toString());
    }

    public boolean isSessionVerified(String sessionId, String email, String otpType) {
        String key = OTP_SESSION_PREFIX + sessionId;
        Object storedEmail = redisTemplate.opsForHash().get(key, "email");
        Object storedType = redisTemplate.opsForHash().get(key, "otpType");
        Object storedVerified = redisTemplate.opsForHash().get(key, "verified");

        return storedEmail != null
                && storedType != null
                && email.equals(storedEmail.toString())
                && otpType.equals(storedType.toString())
                && Boolean.parseBoolean(String.valueOf(storedVerified));
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(OTP_SESSION_PREFIX + sessionId);
    }

    public void deleteSessionByTypeAndEmail(String sessionId, String email, String otpType) {
        String key = OTP_SESSION_PREFIX + sessionId;
        Object storedEmail = redisTemplate.opsForHash().get(key, "email");
        Object storedType = redisTemplate.opsForHash().get(key, "otpType");
        if (storedEmail != null && storedType != null
                && email.equals(storedEmail.toString())
                && otpType.equals(storedType.toString())) {
            redisTemplate.delete(key);
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}