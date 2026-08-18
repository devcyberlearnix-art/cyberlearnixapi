package com.user.register.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for POST /api/v1/users/email/verify-new (Step 3).
 * Submits the OTP that was sent to the user's requested new email address.
 * The old-email OTP must already be verified before this step is accepted.
 */
@Data
public class VerifyNewEmailDto {

    /** The UUID of the EmailChangeRequest (session ID) created in Step 1. */
    @NotBlank(message = "Session ID must not be blank")
    private String sessionId;

    /** 6-digit OTP received on the new email. */
    @NotBlank(message = "OTP must not be blank")
    private String otp;
}
