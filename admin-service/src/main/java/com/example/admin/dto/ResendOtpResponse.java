package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResendOtpResponse {
    private boolean success;
    private String message;
    private OtpData data;
    private String timestamp;

    @Data
    @Builder
    public static class OtpData {
        private int validForMinutes;
        private String otpType;
        private String email; // encrypted
        private String expiresAt;
        private int attemptsLeft;
        private long cooldownSeconds;
    }
}
