package com.example.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaIdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "kafka:processed:";
    private static final Duration TTL = Duration.ofHours(24);

    /**
     * Atomically checks and sets the event ID in Redis.
     *
     * @param eventId the unique event identifier
     * @return true if this is the FIRST time the event is being processed; false if already processed
     */
    public boolean isFirstTime(UUID eventId) {
        if (eventId == null) {
            return true;
        }
        try {
            String key = KEY_PREFIX + eventId;
            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "PROCESSED", TTL);
            boolean firstTime = Boolean.TRUE.equals(isNew);
            if (!firstTime) {
                log.warn("Duplicate event detected and skipped by idempotency guard: eventId={}", eventId);
            }
            return firstTime;
        } catch (Exception e) {
            log.error("Redis idempotency check failed for eventId={}. Falling back to processing.", eventId, e);
            return true;
        }
    }
}
