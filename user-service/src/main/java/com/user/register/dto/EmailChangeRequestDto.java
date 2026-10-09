package com.user.register.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for POST /api/v1/users/email/change-request (Step 1).
 *
 * <p>Re-authentication strategy (pick one):
 * <ul>
 *   <li>{@code password} — the user's current account password (BCrypt-verified).</li>
 *   <li>{@code reAuthOtpSessionId} — a verified Redis OTP session ID from the dedicated
 *       re-auth OTP endpoint (type {@code EMAIL_CHANGE_REAUTH}).</li>
 * </ul>
 * Exactly one of the two must be present; the service validates this.
 */
@Data
public class EmailChangeRequestDto {

    /** The desired new email address. Must be unique across users. */
    @NotBlank(message = "New email must not be blank")
    @Email(message = "New email must be a valid email address")
    private String newEmail;

    /**
     * Option A — re-authenticate via current account password.
     * Mutually exclusive with {@code reAuthOtpSessionId}.
     */
    private String password;

    /**
     * Option B — re-authenticate via a pre-verified OTP session sent to the
     * current email. Pass the session ID returned by the reauth-OTP endpoint.
     * Mutually exclusive with {@code password}.
     */
    private String reAuthOtpSessionId;
}
