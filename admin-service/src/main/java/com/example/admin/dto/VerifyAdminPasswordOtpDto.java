package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for Step 2 of the admin password change flow (OTP verification).
 */
@Data
public class VerifyAdminPasswordOtpDto {

    @NotBlank(message = "Session ID is required.")
    private String sessionId;

    @NotBlank(message = "OTP is required.")
    private String otp;
}
