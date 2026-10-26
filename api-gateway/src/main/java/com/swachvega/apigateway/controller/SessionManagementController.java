package com.swachvega.apigateway.controller;

import com.swachvega.apigateway.model.SessionManagementResponseDTO;
import com.swachvega.apigateway.security.SimpleJwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionManagementController {

    private final WebClient.Builder webClientBuilder;
    private final SimpleJwtTokenProvider jwtTokenProvider;

    @Value("${userservice.url:http://swachvega-userservice:8080}")
    private String userServiceUrl;

    private Mono<String> resolveUserId(ServerWebExchange exchange, String headerUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return Mono.just(headerUserId);
        }
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = jwtTokenProvider.extractTokenFromHeader(authHeader);
        if (token == null || token.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token"));
        }
        return jwtTokenProvider.validateAccessToken(token)
                .map(claims -> {
                    Object sub = claims.get("sub");
                    if (sub == null) {
                        sub = claims.get("userId");
                    }
                    if (sub != null) {
                        return sub.toString();
                    }
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token subject");
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token")));
    }

    /**
     * Get all active sessions for the current user
     */
    @GetMapping
    public Mono<ResponseEntity<SessionManagementResponseDTO>> getUserSessions(
            ServerWebExchange exchange,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {

        return resolveUserId(exchange, userId).flatMap(resolvedUserId -> {
            log.info("Fetching sessions for user: {}", resolvedUserId);
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            return webClientBuilder.build()
                    .get()
                    .uri(userServiceUrl + "/api/v1/users/me/sessions")
                    .header("X-User-Id", resolvedUserId)
                    .header("X-Session-Id", sessionId != null ? sessionId : "")
                    .headers(h -> {
                        if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    })
                    .retrieve()
                    .bodyToMono(SessionManagementResponseDTO.class)
                    .map(ResponseEntity::ok);
        });
    }

    /**
     * Deactivate a specific session
     */
    @DeleteMapping("/{sessionId}")
    public Mono<ResponseEntity<SessionManagementResponseDTO>> deactivateSession(
            ServerWebExchange exchange,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Session-Id", required = false) String currentSessionId,
            @PathVariable String sessionId) {

        return resolveUserId(exchange, userId).flatMap(resolvedUserId -> {
            log.info("Deactivating session {} for user: {}", sessionId, resolvedUserId);
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            return webClientBuilder.build()
                    .delete()
                    .uri(userServiceUrl + "/api/v1/users/me/sessions/{sessionId}", sessionId)
                    .header("X-User-Id", resolvedUserId)
                    .header("X-Session-Id", currentSessionId != null ? currentSessionId : "")
                    .headers(h -> {
                        if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    })
                    .retrieve()
                    .bodyToMono(SessionManagementResponseDTO.class)
                    .map(ResponseEntity::ok);
        });
    }

    /**
     * Deactivate all other sessions except the current one
     */
    @PostMapping("/deactivate-others")
    public Mono<ResponseEntity<SessionManagementResponseDTO>> deactivateOtherSessions(
            ServerWebExchange exchange,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-Session-Id", required = false) String currentSessionId) {

        return resolveUserId(exchange, userId).flatMap(resolvedUserId -> {
            log.info("Deactivating other sessions for user: {}", resolvedUserId);
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            return webClientBuilder.build()
                    .post()
                    .uri(userServiceUrl + "/api/v1/users/me/sessions/deactivate-others")
                    .header("X-User-Id", resolvedUserId)
                    .header("X-Session-Id", currentSessionId != null ? currentSessionId : "")
                    .headers(h -> {
                        if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    })
                    .retrieve()
                    .bodyToMono(SessionManagementResponseDTO.class)
                    .map(ResponseEntity::ok);
        });
    }

    /**
     * Deactivate all sessions for the user (logout from all devices)
     */
    @PostMapping("/deactivate-all")
    public Mono<ResponseEntity<SessionManagementResponseDTO>> deactivateAllSessions(
            ServerWebExchange exchange,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        return resolveUserId(exchange, userId).flatMap(resolvedUserId -> {
            log.info("Deactivating all sessions for user: {}", resolvedUserId);
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            return webClientBuilder.build()
                    .delete()
                    .uri(userServiceUrl + "/api/v1/users/me/sessions")
                    .header("X-User-Id", resolvedUserId)
                    .headers(h -> {
                        if (authHeader != null) h.set(HttpHeaders.AUTHORIZATION, authHeader);
                    })
                    .retrieve()
                    .bodyToMono(SessionManagementResponseDTO.class)
                    .map(ResponseEntity::ok);
        });
    }
}

