package com.example.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginOtpResponse {
    private boolean success;
    private String message;
    private String timestamp;
    private OtpData  data;

    @Data
    @Builder
    public static class OtpData{
        private int validForMinutes;
        private String otpType;
        private String email;
        private String expiresAt;
        private int cooldownSeconds;
    }
}
