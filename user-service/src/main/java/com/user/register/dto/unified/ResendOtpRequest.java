package com.user.register.dto.unified;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpRequest {
    @NotBlank(message = "OTP session is required")
    private String otpSessionId;
}
