package com.user.register.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for POST /api/v1/users/email/verify-old (Step 2).
 * Submits the OTP that was sent to the user's current (old) email.
 */
@Data
public class VerifyOldEmailDto {

    /** The UUID of the EmailChangeRequest (session ID) created in Step 1. */
    @NotBlank(message = "Session ID must not be blank")
    private String sessionId;

    /** 6-digit OTP received on the old email. */
    @NotBlank(message = "OTP must not be blank")
    private String otp;
}
