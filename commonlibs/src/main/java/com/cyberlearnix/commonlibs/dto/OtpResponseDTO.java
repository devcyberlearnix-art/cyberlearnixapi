package com.cyberlearnix.commonlibs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for OTP send requests")
public class OtpResponseDTO {
    
    @Schema(description = "Indicates if OTP was sent successfully", example = "true")
    private boolean success;
    
    @Schema(description = "Status message", example = "OTP sent successfully")
    private String message;
    
    @Schema(description = "Session ID for OTP verification", example = "session_123456")
    private String otpSessionId;
    
    @Schema(description = "OTP expiry time in seconds", example = "300")
    private Integer expirySeconds;
    
    @Schema(description = "Delivery method used", example = "SMS")
    private String deliveryMethod;
    
    @Schema(description = "Masked contact information", example = "+91*****43210")
    private String maskedContact;
    
    @Schema(description = "Cooldown period before resend is allowed (in seconds)", example = "60")
    private Integer resendCooldownSeconds;
}
