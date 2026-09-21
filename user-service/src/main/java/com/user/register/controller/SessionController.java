package com.user.register.controller;

import com.user.register.dto.ApiResponse;
import com.user.register.dto.LogoutAllResponse;
import com.user.register.dto.LogoutResponse;
import com.user.register.dto.SessionDto;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;
import com.user.register.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.user.register.entity.UserSession;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/v1/users/me/sessions")
public class SessionController {

    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SessionService sessionService;
    private byte[] secretKey;

    public SessionController(UserSessionRepository sessionRepository,
                             UserRepository userRepository,
                             JwtUtil jwtUtil,
                             SessionService sessionService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.sessionService = sessionService;
    }

    @GetMapping
    public ApiResponse<List<SessionDto>> listSessions(HttpServletRequest request) {
        // Resolve the authenticated user
        UUID userId = resolveAuthenticatedUserId(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch sessions via SessionService (ensures any future business logic is applied)
        List<UserSession> userSessions = sessionService.getSessionsForUser(user);
        log.info("Fetched {} sessions for user {}", userSessions.size(), userId);

        // Transform to DTO, filtering out expired sessions
        List<SessionDto> sessions = userSessions.stream()
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(s -> new SessionDto(
                        s.getId(),
                        user.getId(),
                        s.getDeviceInfo(),
                        s.getIpAddress(),
                        s.getCreatedAt(),
                        user.getEmail()
                ))
                .toList();

        return new ApiResponse<>(
                true,
                "Sessions fetched successfully",
                sessions,
                LocalDateTime.now()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> logoutDevice(
            @PathVariable("id") UUID sessionId,
            HttpServletRequest request) {

        try {

            LogoutResponse response = sessionService.logoutDevice(sessionId, request);

            return ResponseEntity.status(200).body(
                    new ApiResponse<>(
                            true,
                            "Device logged out successfully",
                            response,
                            LocalDateTime.now()
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.status(404).body(
                    new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null,
                            LocalDateTime.now()
                    )
            );
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Object>> logoutAllSessions(HttpServletRequest request) {

        try {

            UUID userId = resolveAuthenticatedUserId(request);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<SessionDto> revokedSessions = sessionService.logoutAllSessions(user);

            LogoutAllResponse response = new LogoutAllResponse(
                    user.getId(),
                    revokedSessions.size(),
                    revokedSessions,
                    LocalDateTime.now()
            );

            return ResponseEntity.status(200).body(
                    new ApiResponse<>(
                            true,
                            "Logged out from all sessions successfully",
                            response,
                            LocalDateTime.now()
                    )
            );

        } catch (RuntimeException e) {

            return ResponseEntity.status(400).body(
                    new ApiResponse<>(
                            false,
                            e.getMessage(),
                            null,
                            LocalDateTime.now()
                    )
            );
        }
    }

    /**
     * Resolves the authenticated user's UUID from SecurityContext (gateway headers)
     * with fallback to JWT parsing for direct access.
     */
    private UUID resolveAuthenticatedUserId(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String principal) {
            try {
                return UUID.fromString(principal);
            } catch (IllegalArgumentException ignored) { }
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return UUID.fromString(jwtUtil.validateAccessTokenAndGetUserId(token));
        }
        throw new RuntimeException("Missing authentication");
    }
}
