package com.example.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Step 1 — Initiate admin email change.
 * Exactly one of {@code password} or {@code reAuthOtpSessionId} must be provided.
 */
@Data
public class AdminEmailChangeRequestDto {

    /** The new email address the admin wishes to use. */
    @NotBlank(message = "New email is required.")
    @Email(message = "New email must be a valid email address.")
    private String newEmail;

    /**
     * Option A: Current password for re-authentication.
     * Mutually exclusive with {@code reAuthOtpSessionId}.
     */
    private String password;

    /**
     * Option B: A verified OTP session ID obtained by calling the login OTP endpoints.
     * Mutually exclusive with {@code password}.
     */
    private String reAuthOtpSessionId;
}
