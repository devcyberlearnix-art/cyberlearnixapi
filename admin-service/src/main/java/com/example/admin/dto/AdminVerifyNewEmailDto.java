package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Step 3 — Verify OTP sent to the admin's new email address (final step).
 * On success the admin's email is updated and all sessions are invalidated.
 */
@Data
public class AdminVerifyNewEmailDto {

    /** The session ID returned from Step 1 (initiate). */
    @NotBlank(message = "Session ID is required.")
    private String sessionId;

    /** The 6-digit OTP received on the new email. */
    @NotBlank(message = "OTP is required.")
    private String otp;
}
