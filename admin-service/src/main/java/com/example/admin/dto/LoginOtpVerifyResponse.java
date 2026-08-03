package com.example.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginOtpVerifyResponse {
    private boolean success;
    private String message;
    private LoginOtpData data;
    private String timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginOtpData {
        private String email;
        private String accessToken;
        private String refreshToken;
        private String role;
        private String expiresAt; // OTP verification timestamp
    }
}