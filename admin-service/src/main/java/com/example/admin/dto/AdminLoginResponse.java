package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AdminLoginResponse {

    private AdminInfo admin;
    private AuthenticationInfo authentication;
    private SessionInfo sessionInfo;

    @Data
    @Builder
    public static class AdminInfo {
        private UUID id;
        private String email;
        private String role;
        private String adminType;
        private String assignedService;
        private String firstName;
        private String lastName;
        private String mobileNumber;
    }

    @Data
    @Builder
    public static class AuthenticationInfo {
        private String accessToken;
        private String accessTokenExpiresIn;
        private String refreshToken;
        private String refreshTokenExpiresIn;
    }

    @Data
    @Builder
    public static class SessionInfo {
        private String loginTime;
        private String ipAddress;
        private String device;
    }
}