package com.user.register.service;

import com.user.register.security.UnifiedJwtService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@Slf4j
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final UnifiedJwtService unifiedJwtService;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    public TokenBlacklistService(StringRedisTemplate redisTemplate, UnifiedJwtService unifiedJwtService) {
        this.redisTemplate = redisTemplate;
        this.unifiedJwtService = unifiedJwtService;
    }

    public void blacklistToken(String token) {
        if (token == null || token.isBlank()) {
            return;
        }

        long ttlSeconds = 900; // default 15 minutes (access token duration)
        try {
            Claims claims = unifiedJwtService.extractClaims(token);
            Date expiration = claims.getExpiration();
            if (expiration != null) {
                long diff = expiration.getTime() - System.currentTimeMillis();
                if (diff > 0) {
                    ttlSeconds = diff / 1000;
                } else {
                    // Token is already expired, blacklist it briefly
                    ttlSeconds = 60;
                }
            }

            // Check if it's a refresh token
            String type = claims.get("type", String.class);
            if ("refresh".equals(type)) {
                if (expiration == null) {
                    ttlSeconds = 30L * 24 * 60 * 60; // 30 days fallback
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse token claims to calculate TTL for blacklist, using fallback", e);
            ttlSeconds = 30L * 24 * 60 * 60; // fallback to 30 days to cover refresh tokens
        }

        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "true", Duration.ofSeconds(ttlSeconds));
        log.info("Token blacklisted in Redis with TTL of {} seconds", ttlSeconds);
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}