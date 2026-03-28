package com.swachvega.apigateway.service;

import com.swachvega.apigateway.model.AuthRequest;
import com.swachvega.apigateway.model.AuthResponse;
import com.swachvega.apigateway.model.SessionInfo;
import com.swachvega.apigateway.security.SimpleJwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final WebClient.Builder webClientBuilder;
    private final SimpleJwtTokenProvider jwtTokenProvider;
    private final SessionService sessionService;

        @Value("${userservice.url:http://swachvega-userservice:8080}")
    private String userServiceUrl;

    /**
     * Handle login - delegates to userservice for validation, manages tokens in gateway
     */
    public Mono<AuthResponse> login(AuthRequest authRequest) {
        // Step 1: Validate credentials with UserService
        return validateWithUserService(authRequest)
                .flatMap(userInfo -> {
                    // Step 2: Generate session and tokens
                    String sessionId = UUID.randomUUID().toString();
                    
                    // Step 3: Generate tokens
                    String accessToken = jwtTokenProvider.generateAccessToken(
                            userInfo.getUserId(), 
                            userInfo.getUsername(), 
                            sessionId, 
                            userInfo.getRole()
                    );
                    
                    String refreshToken = jwtTokenProvider.generateRefreshToken(
                            userInfo.getUserId(), 
                            sessionId
                    );
                    
                    // Step 4: Store session info
                    return sessionService.createSession(sessionId, userInfo, authRequest.getDeviceId(), authRequest.getDeviceName())
                            .then(Mono.just(AuthResponse.builder()
                                    .success(true)
                                    .message("Login successful")
                                    .accessToken(accessToken)
                                    .refreshToken(refreshToken)
                                    .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                    .tokenType("Bearer")
                                    .sessionId(sessionId)
                                    .user(userInfo)
                                    .build()));
                })
                .doOnSuccess(response -> log.info("User {} logged in successfully", authRequest.getName()))
                .doOnError(error -> log.error("Login failed for user {}: {}", authRequest.getName(), error.getMessage()));
    }

    /**
     * Refresh access token using refresh token
     */
    public Mono<AuthResponse> refreshToken(String refreshToken) {
        return jwtTokenProvider.validateRefreshToken(refreshToken)
                .flatMap(claims -> {
                    String sessionId = (String) claims.get("sessionId");
                    String userId = (String) claims.get("sub");
                    
                    // Verify session is still active
                    return sessionService.getSession(sessionId)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session expired")))
                            .flatMap(sessionInfo -> {
                                // Generate new access token
                                String newAccessToken = jwtTokenProvider.generateAccessToken(
                                        userId,
                                        sessionInfo.getUsername(),
                                        sessionId,
                                        sessionInfo.getRole()
                                );
                                
                                return Mono.just(AuthResponse.builder()
                                        .success(true)
                                        .message("Token refreshed")
                                        .accessToken(newAccessToken)
                                        .refreshToken(refreshToken) // Keep same refresh token
                                        .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                                        .tokenType("Bearer")
                                        .sessionId(sessionId)
                                        .user(AuthResponse.UserInfo.builder()
                                                .userId(sessionInfo.getUserId())
                                                .username(sessionInfo.getUsername())
                                                .email(sessionInfo.getEmail())
                                                .role(sessionInfo.getRole())
                                                .build())
                                        .build());
                            });
                })
                .doOnSuccess(response -> log.info("Token refreshed for session {}", response.getSessionId()))
                .doOnError(error -> log.error("Token refresh failed: {}", error.getMessage()));
    }

    /**
     * Logout - revoke session and tokens
     */
    public Mono<Void> logout(String sessionId) {
        return sessionService.revokeSession(sessionId)
                .doOnSuccess(v -> log.info("User logged out, session {} revoked", sessionId))
                .doOnError(error -> log.error("Logout failed for session {}: {}", sessionId, error.getMessage()));
    }

    /**
     * Logout from all devices
     */
    public Mono<Void> logoutAllDevices(String userId) {
        return sessionService.revokeAllUserSessions(userId)
                .doOnSuccess(v -> log.info("All sessions revoked for user {}", userId))
                .doOnError(error -> log.error("Logout all devices failed for user {}: {}", userId, error.getMessage()));
    }

    /**
     * Validate credentials with UserService
     */
    private Mono<AuthResponse.UserInfo> validateWithUserService(AuthRequest authRequest) {
        return webClientBuilder.build()
                .post()
                .uri(userServiceUrl + "/api/consumer/auth/login")
                .bodyValue(authRequest)
                .retrieve()
                .onStatus(HttpStatus.UNAUTHORIZED::equals, 
                    response -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .bodyToMono(UserServiceAuthResponse.class)
                .map(response -> AuthResponse.UserInfo.builder()
                        .userId(response.getUserId())
                        .username(response.getUsername())
                        .email(response.getEmail())
                        .role(response.getRole())
                        .build())
                .onErrorMap(Exception.class, ex -> 
                    new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed", ex));
    }

    // Internal DTO for UserService response
    private static class UserServiceAuthResponse {
        private String userId;
        private String username;
        private String email;
        private String role;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
