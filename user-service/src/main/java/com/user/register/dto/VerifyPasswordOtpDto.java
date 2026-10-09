package com.user.register.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for Step 2 of the password change flow (OTP verification).
 */
@Data
public class VerifyPasswordOtpDto {

    @NotBlank(message = "Session ID is required.")
    private String sessionId;

    @NotBlank(message = "OTP is required.")
    private String otp;
}
