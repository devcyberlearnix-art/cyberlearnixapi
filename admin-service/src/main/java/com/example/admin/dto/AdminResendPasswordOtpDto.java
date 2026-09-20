package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for resending admin password change OTP (logged in).
 * Accepts either sessionId or otpSessionId.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResendPasswordOtpDto {

    @JsonAlias({"otpSessionId"})
    private String sessionId;

    private String otpSessionId;

    public String resolveSessionId() {
        if (sessionId != null && !sessionId.isBlank()) {
            return sessionId.trim();
        }
        if (otpSessionId != null && !otpSessionId.isBlank()) {
            return otpSessionId.trim();
        }
        return null;
    }
}
