package com.user.register.dto.unified;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {

    private boolean success;
    private String message;
    private AuthenticationInfo authentication;
    private LocalDateTime timestamp;

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
}
