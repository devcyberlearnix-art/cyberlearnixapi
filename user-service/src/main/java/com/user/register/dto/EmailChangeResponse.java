package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Unified response payload returned by all three email-change endpoints.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailChangeResponse {

    /** The UUID of the EmailChangeRequest (session ID). Clients must persist this. */
    private String sessionId;

    /** Whether the OTP sent to the old email has been successfully verified. */
    private boolean oldEmailVerified;

    /** Whether the OTP sent to the new email has been successfully verified. */
    private boolean newEmailVerified;

    /**
     * Current status of the request: PENDING, VERIFIED, or EXPIRED.
     */
    private String status;

    /** When this email-change request expires. */
    private LocalDateTime expiresAt;

    /**
     * Human-readable message describing the outcome of the current step.
     * Populated by the service layer.
     */
    private String message;

    /**
     * Populated only on final success (status = VERIFIED).
     * Signals the client to redirect to login.
     */
    private Boolean requiresReLogin;
}
