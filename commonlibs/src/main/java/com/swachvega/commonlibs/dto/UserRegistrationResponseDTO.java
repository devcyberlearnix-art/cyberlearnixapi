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
@Schema(description = "Response DTO for user registration completion")
public class UserRegistrationResponseDTO {
    
    @Schema(description = "Indicates if registration was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Status message", example = "Registration completed successfully")
    private String message;
    
    @Schema(description = "User information")
    private UserResponseDTO user;
    
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;
    
    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "Session ID for managing multiple sessions", example = "session_123456")
    private String sessionId;
    
    @Schema(description = "Token expiry time in seconds", example = "3600")
    private long expiresIn;
}
