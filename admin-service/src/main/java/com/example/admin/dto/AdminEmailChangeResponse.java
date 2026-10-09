package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Unified response object returned by all admin email-change endpoints.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminEmailChangeResponse {

    /** The UUID of the email-change session (matches AdminEmailChangeRequest.id). */
    private String sessionId;

    /** True once the old-email OTP has been successfully verified. */
    private boolean oldEmailVerified;

    /** True once the new-email OTP has been successfully verified. */
    private boolean newEmailVerified;

    /** PENDING | VERIFIED | EXPIRED */
    private String status;

    /** When this session expires / expired. */
    private LocalDateTime expiresAt;

    /** Human-readable description of the current state. */
    private String message;

    /**
     * Present (and {@code true}) only on the final success response.
     * Signals to the client that all sessions have been revoked
     * and the admin must log in again.
     */
    private Boolean requiresReLogin;
}
