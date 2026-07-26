package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForgotPasswordResponse {
    private boolean success;
    private String message;
    private ForgotPasswordData data;
    private String timestamp;

    @Data
    @Builder
    public static class ForgotPasswordData {
        private String email;
        private String otpType; // "password_reset"
        private int validForMinutes;
        private String expiresAt;
        private int cooldownSeconds;
    }
}
