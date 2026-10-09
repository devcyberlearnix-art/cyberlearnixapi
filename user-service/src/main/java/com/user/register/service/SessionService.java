package com.user.register.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.user.register.dto.LogoutResponse;
import com.user.register.dto.SessionDto;
import com.user.register.entity.UserSession;
import com.user.register.entity.User;
import com.user.register.repository.UserRepository;
import com.user.register.repository.UserSessionRepository;
import com.user.register.security.JwtUtil;

import com.user.register.util.DynamicDeviceAndLocationResolver;
import com.user.register.util.DynamicDeviceAndLocationResolver.ClientContext;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL) // ignore nulls

@Slf4j
@Service
public class SessionService {

    // ✅ Declare ALL required dependencies
    private final JwtUtil jwtUtil;
    private final UserSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final TokenBlacklistService blacklistService;
    private final DynamicDeviceAndLocationResolver deviceResolver;

    // ✅ Constructor injection for ALL dependencies
    public SessionService(JwtUtil jwtUtil,
                          UserSessionRepository sessionRepository,
                          UserRepository userRepository,
                          TokenBlacklistService blacklistService,
                          DynamicDeviceAndLocationResolver deviceResolver) {
        this.jwtUtil = jwtUtil;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.blacklistService = blacklistService;
        this.deviceResolver = deviceResolver;
    }

    // Create session at login with tokens & dynamic device/location resolution
    public UserSession createSession(User user, HttpServletRequest request, String accessToken, String refreshToken) {
        return createSession(user, request, null, accessToken, refreshToken);
    }

    public UserSession createSession(User user, HttpServletRequest request, HttpServletResponse response, String accessToken, String refreshToken) {
        ClientContext client = deviceResolver.resolve(request, response);

        UserSession session = new UserSession();
        session.setUser(user);
        session.setDeviceInfo(request != null ? request.getHeader("User-Agent") : "Unknown");
        session.setDeviceId(client.getDeviceId());
        session.setDeviceName(client.getDeviceName());
        session.setDeviceType(client.getDeviceType());
        session.setBrowser(client.getBrowser());
        session.setOperatingSystem(client.getOperatingSystem());
        session.setLatitude(client.getLatitude());
        session.setLongitude(client.getLongitude());
        session.setCity(client.getCity());
        session.setCountry(client.getCountry());
        session.setIpAddress(client.getIpAddress());
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusDays(30));
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);

        UserSession saved = sessionRepository.save(session);
        log.info("Created dynamic session for user {} [Device: {}, Location: {}, {} ({}, {})]",
                user.getId(), client.getDeviceName(), client.getCity(), client.getCountry(), client.getLatitude(), client.getLongitude());
        return saved;
    }

    public List<UserSession> getSessionsForUser(User user) {
        return sessionRepository.findByUser(user);
    }

    public java.util.Optional<UserSession> findByRefreshToken(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken);
    }

    @Transactional
    public void rotateTokens(String oldRefreshToken, String accessToken, String refreshToken) {
        UserSession session = sessionRepository.findForUpdateByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (session.getAccessToken() != null) {
            blacklistService.blacklistToken(session.getAccessToken());
        }
        blacklistService.blacklistToken(oldRefreshToken);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        sessionRepository.save(session);
    }

    // Logout single device
    public LogoutResponse logoutDevice(UUID sessionId, HttpServletRequest request) {

        UserSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        User user = session.getUser();

        // Blacklist tokens before deleting session
        if (session.getAccessToken() != null) {
            blacklistService.blacklistToken(session.getAccessToken());
        }
        if (session.getRefreshToken() != null) {
            blacklistService.blacklistToken(session.getRefreshToken());
        }

        LogoutResponse response = new LogoutResponse(
                user.getId(),
                user.getEmail(),
                session.getDeviceInfo(),
                getClientIp(request),   // ✅ correct IP extraction
                LocalDateTime.now()
        );

        sessionRepository.delete(session);

        return response;
    }

    public List<SessionDto> logoutAllSessions(User user) {

        List<UserSession> activeSessions = sessionRepository.findByUser(user)
                .stream()
                .filter(s -> s.getExpiresAt() == null || s.getExpiresAt().isAfter(LocalDateTime.now()))
                .toList();

        // Blacklist all tokens before deleting sessions
        for (UserSession session : activeSessions) {
            if (session.getAccessToken() != null) {
                blacklistService.blacklistToken(session.getAccessToken());
            }
            if (session.getRefreshToken() != null) {
                blacklistService.blacklistToken(session.getRefreshToken());
            }
        }

        List<SessionDto> revokedSessions = activeSessions.stream()
                .map(s -> SessionDto.builder()
                        .id(s.getId())
                        .userId(user.getId())
                        .email(user.getEmail())
                        .deviceInfo(s.getDeviceInfo())
                        .deviceId(s.getDeviceId())
                        .deviceName(s.getDeviceName())
                        .deviceType(s.getDeviceType())
                        .browser(s.getBrowser())
                        .operatingSystem(s.getOperatingSystem())
                        .latitude(s.getLatitude())
                        .longitude(s.getLongitude())
                        .city(s.getCity())
                        .country(s.getCountry())
                        .ipAddress(s.getIpAddress())
                        .loginTime(s.getCreatedAt())
                        .build()
                )
                .toList();

        sessionRepository.deleteAll(activeSessions);

        return revokedSessions;
    }

    @Transactional
    public int invalidateAllSessionsForUser(User user) {
        List<UserSession> existingSessions = sessionRepository.findByUser(user);
        for (UserSession session : existingSessions) {
            if (session.getAccessToken() != null) {
                blacklistService.blacklistToken(session.getAccessToken());
            }
            if (session.getRefreshToken() != null) {
                blacklistService.blacklistToken(session.getRefreshToken());
            }
        }
        sessionRepository.deleteAll(existingSessions);
        return existingSessions.size();
    }

    private String getClientIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
