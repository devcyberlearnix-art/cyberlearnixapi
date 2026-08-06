package com.user.register.dto.unified;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private boolean success;
    private String message;
    private UserData user;
    private AuthenticationInfo authentication;
    private SessionInfo sessionInfo;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserData {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
        private String mobileNumber;
        private String role;
        private String adminType;
        private String assignedService;
        private List<String> permissions;
        private boolean verified;
        private boolean approved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthenticationInfo {
        private String accessToken;
        private String accessTokenExpiresIn;
        private String refreshToken;
        private String refreshTokenExpiresIn;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionInfo {
        private String loginTime;
        private String ipAddress;
        private String device;
    }
}
