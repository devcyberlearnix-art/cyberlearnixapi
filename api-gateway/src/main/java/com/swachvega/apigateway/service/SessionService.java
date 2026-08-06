package com.swachvega.apigateway.service;

import com.swachvega.apigateway.model.AuthResponse;
import com.swachvega.apigateway.model.SessionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    private static final Duration SESSION_TIMEOUT = Duration.ofDays(30); // Refresh token lifetime

    /**
     * Create a new session
     */
    public Mono<Void> createSession(String sessionId, AuthResponse.UserInfo userInfo, String deviceId, String deviceName) {
        SessionInfo sessionInfo = SessionInfo.builder()
                .sessionId(sessionId)
                .userId(userInfo.getUserId())
                .username(userInfo.getUsername())
                .email(userInfo.getEmail())
                .role(userInfo.getRole())
                .deviceId(deviceId)
                .deviceName(deviceName)
                .createdAt(Instant.now())
                .lastAccessedAt(Instant.now())
                .active(true)
                .build();

        return Mono.zip(
                // Store session info
                redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, sessionInfo, SESSION_TIMEOUT),
                // Add to user's session list
                redisTemplate.opsForSet().add(USER_SESSIONS_PREFIX + userInfo.getUserId(), sessionId)
        ).then()
        .doOnSuccess(v -> log.debug("Session created: {}", sessionId))
        .doOnError(error -> log.error("Failed to create session {}: {}", sessionId, error.getMessage()));
    }

    /**
     * Get session information
     */
    public Mono<SessionInfo> getSession(String sessionId) {
        return redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId)
                .cast(SessionInfo.class)
                .doOnNext(session -> {
                    if (session != null) {
                        // Update last accessed time
                        session.setLastAccessedAt(Instant.now());
                        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, session, SESSION_TIMEOUT)
                                .subscribe();
                    }
                })
                .doOnError(error -> log.error("Failed to get session {}: {}", sessionId, error.getMessage()));
    }

    /**
     * Check if session is active
     */
    public Mono<Boolean> isSessionActive(String sessionId) {
        return getSession(sessionId)
                .map(SessionInfo::isActive)
                .defaultIfEmpty(false);
    }

    /**
     * Revoke a specific session
     */
    public Mono<Void> revokeSession(String sessionId) {
        return getSession(sessionId)
                .flatMap(session -> Mono.zip(
                        // Remove session
                        redisTemplate.opsForValue().delete(SESSION_PREFIX + sessionId),
                        // Remove from user's session list
                        redisTemplate.opsForSet().remove(USER_SESSIONS_PREFIX + session.getUserId(), sessionId)
                ).then())
                .doOnSuccess(v -> log.info("Session revoked: {}", sessionId))
                .doOnError(error -> log.error("Failed to revoke session {}: {}", sessionId, error.getMessage()));
    }

    /**
     * Revoke all sessions for a user
     */
    public Mono<Void> revokeAllUserSessions(String userId) {
        return redisTemplate.opsForSet().members(USER_SESSIONS_PREFIX + userId)
                .cast(String.class)
                .flatMap(sessionId -> redisTemplate.opsForValue().delete(SESSION_PREFIX + sessionId))
                .then(redisTemplate.opsForValue().delete(USER_SESSIONS_PREFIX + userId))
                .then()
                .doOnSuccess(v -> log.info("All sessions revoked for user: {}", userId))
                .doOnError(error -> log.error("Failed to revoke all sessions for user {}: {}", userId, error.getMessage()));
    }

    /**
     * Get all active sessions for a user
     */
    public Flux<SessionInfo> getUserSessions(String userId) {
        return redisTemplate.opsForSet().members(USER_SESSIONS_PREFIX + userId)
                .cast(String.class)
                .flatMap(sessionId -> getSession(sessionId))
                .filter(session -> session != null && session.isActive());
    }

    /**
     * Clean up expired sessions (scheduled task)
     */
    public Mono<Void> cleanupExpiredSessions() {
        // This would be called by a scheduled task
        // For now, Redis TTL handles expiration automatically
        return Mono.empty();
    }
}
