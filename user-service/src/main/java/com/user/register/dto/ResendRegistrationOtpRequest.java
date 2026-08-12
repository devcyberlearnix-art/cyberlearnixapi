package com.user.register.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendRegistrationOtpRequest {
    @NotBlank(message = "OTP session is required")
    private String otpSessionId;
}