package com.swachvega.apigateway.controller;

import com.swachvega.apigateway.model.SessionManagementResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionManagementController {

    private final WebClient.Builder webClientBuilder;

    @Value("${userservice.url:http://swachvega-userservice:8080}")
    private String userServiceUrl;

    /**
     * Get all active sessions for the current user
     */
    @GetMapping
    public Mono<ResponseEntity<SessionManagementResponseDTO>> getUserSessions(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Session-Id") String sessionId) {
        
        log.info("Fetching sessions for user: {}", userId);
        
        return webClientBuilder.build()
                .get()
                .uri(userServiceUrl + "/api/consumer/sessions")
                .header("X-User-Id", userId)
                .header("X-Session-Id", sessionId)
                .retrieve()
                .bodyToMono(SessionManagementResponseDTO.class)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    log.error("Failed to fetch sessions for user {}: {}", userId, ex.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Deactivate a specific session
     */
    @DeleteMapping("/{sessionId}")
    public Mono<ResponseEntity<SessionManagementResponseDTO>> deactivateSession(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Session-Id") String currentSessionId,
            @PathVariable String sessionId) {
        
        log.info("Deactivating session {} for user: {}", sessionId, userId);
        
        return webClientBuilder.build()
                .delete()
                .uri(userServiceUrl + "/api/consumer/sessions/{sessionId}", sessionId)
                .header("X-User-Id", userId)
                .header("X-Session-Id", currentSessionId)
                .retrieve()
                .bodyToMono(SessionManagementResponseDTO.class)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    log.error("Failed to deactivate session {} for user {}: {}", sessionId, userId, ex.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Deactivate all other sessions except the current one
     */
    @PostMapping("/deactivate-others")
    public Mono<ResponseEntity<SessionManagementResponseDTO>> deactivateOtherSessions(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Session-Id") String currentSessionId) {
        
        log.info("Deactivating other sessions for user: {}", userId);
        
        return webClientBuilder.build()
                .post()
                .uri(userServiceUrl + "/api/consumer/sessions/deactivate-others")
                .header("X-User-Id", userId)
                .header("X-Session-Id", currentSessionId)
                .retrieve()
                .bodyToMono(SessionManagementResponseDTO.class)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    log.error("Failed to deactivate other sessions for user {}: {}", userId, ex.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Deactivate all sessions for the user (logout from all devices)
     */
    @PostMapping("/deactivate-all")
    public Mono<ResponseEntity<SessionManagementResponseDTO>> deactivateAllSessions(
            @RequestHeader("X-User-Id") String userId) {
        
        log.info("Deactivating all sessions for user: {}", userId);
        
        return webClientBuilder.build()
                .post()
                .uri(userServiceUrl + "/api/consumer/sessions/deactivate-all")
                .header("X-User-Id", userId)
                .retrieve()
                .bodyToMono(SessionManagementResponseDTO.class)
                .map(ResponseEntity::ok)
                .onErrorResume(ex -> {
                    log.error("Failed to deactivate all sessions for user {}: {}", userId, ex.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
