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
@Schema(description = "Response DTO for phone OTP validation")
public class PhoneOtpValidationResponseDTO {
    
    @Schema(description = "Indicates if OTP validation was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Status message", example = "OTP verified successfully")
    private String message;
    
    @Schema(description = "Whether user exists (true for login, false for registration)", example = "true")
    private boolean userExists;
    
    @Schema(description = "Temporary token for completing registration (only for new users)", example = "temp_token_123")
    private String tempToken;
    
    @Schema(description = "User information (only for existing users)")
    private UserResponseDTO user;
    
    @Schema(description = "JWT access token (only for existing users)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "JWT refresh token (only for existing users)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "Session ID for managing multiple sessions", example = "session_123456")
    private String sessionId;
    
    @Schema(description = "Token expiry time in seconds", example = "3600")
    private long expiresIn;
}
