package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resending admin email change OTPs.
 * Accepts either sessionId or otpSessionId.
 * Target can optionally specify "OLD", "NEW", or "ALL" (default: all unverified).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminResendEmailChangeOtpDto {

    @JsonAlias({"otpSessionId"})
    private String sessionId;

    private String otpSessionId;

    @Pattern(regexp = "(?i)^(OLD|NEW|ALL)?$", message = "Target must be OLD, NEW, or ALL")
    private String target;

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
