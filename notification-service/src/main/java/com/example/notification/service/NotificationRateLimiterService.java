package com.example.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis-based sliding rate limiter to prevent notification spamming and inbox bombing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRateLimiterService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.notification.rate-limit.max-alerts:3}")
    private int maxAlertsPerWindow;

    @Value("${app.notification.rate-limit.window-minutes:15}")
    private int windowMinutes;

    private static final String KEY_PREFIX = "ratelimit:notification:login:";

    /**
     * Checks if a login notification is permitted under the rate limit policy.
     *
     * @param userId user identifier
     * @return true if notification can be sent, false if rate limited
     */
    public boolean allowAlert(UUID userId) {
        if (userId == null) {
            return true;
        }

        try {
            String key = KEY_PREFIX + userId;
            Long currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount != null && currentCount == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(windowMinutes));
            }

            if (currentCount != null && currentCount > maxAlertsPerWindow) {
                log.warn("Rate limit exceeded for userId={}. Count={}/{} in {}min window. Suppressing alert spam.",
                        userId, currentCount, maxAlertsPerWindow, windowMinutes);
                return false;
            }

            return true;
        } catch (Exception ex) {
            log.error("Redis rate limiter error for userId={}, failing open to allow critical security alert: {}",
                    userId, ex.getMessage());
            return true; // Fail-open for high-priority security notifications
        }
    }
}
