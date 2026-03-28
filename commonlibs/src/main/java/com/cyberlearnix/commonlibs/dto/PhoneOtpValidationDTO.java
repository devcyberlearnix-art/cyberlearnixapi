package com.cyberlearnix.commonlibs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for validating OTP sent to phone number")
public class PhoneOtpValidationDTO {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+91[6-9]\\d{9}$", message = "Phone number must be a valid Indian mobile number starting with +91")
    @Schema(description = "Phone number in format +91XXXXXXXXXX", example = "+919876543210", required = true)
    private String phone;
    
    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be a 6-digit number")
    @Schema(description = "6-digit OTP sent to the phone", example = "123456", required = true)
    private String otp;
    
    @NotBlank(message = "OTP session ID is required")
    @Schema(description = "Session ID received from the send OTP request", example = "session_123456", required = true)
    private String otpSessionId;
}
