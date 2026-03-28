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
@Schema(description = "User information response DTO")
public class UserResponseDTO {
    
    @Schema(description = "Unique user identifier", example = "user123")
    private String userId;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "Phone number", example = "+919876543210")
    private String phoneNumber;
    
    @Schema(description = "First name", example = "John")
    private String firstName;
    
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    private String fullName;
    
    @Schema(description = "User role", example = "CUSTOMER")
    private String role;
    
    @Schema(description = "Email verification status", example = "true")
    private boolean emailVerified;
    
    @Schema(description = "Phone verification status", example = "true")
    private boolean phoneVerified;

    private String alternatePhone;

    private String bio;



}
