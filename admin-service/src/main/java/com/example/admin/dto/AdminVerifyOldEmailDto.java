package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Step 2 — Verify OTP sent to the admin's current (old) email address.
 */
@Data
public class AdminVerifyOldEmailDto {

    /** The session ID returned from Step 1 (initiate). */
    @NotBlank(message = "Session ID is required.")
    private String sessionId;

    /** The 6-digit OTP received on the old (current) email. */
    @NotBlank(message = "OTP is required.")
    private String otp;
}
