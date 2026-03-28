package com.cyberlearnix.commonlibs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for completing user registration after OTP verification")
public class UserRegistrationCompleteDTO {
    
    @NotBlank(message = "Temporary token is required")
    @Schema(description = "Temporary token from OTP validation step", example = "temp_token_123", required = true)
    private String tempToken;
    
    @NotBlank(message = "Name is required")
    @Schema(description = "Full name of the user", example = "John Doe", required = true)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email address of the user", example = "john.doe@example.com", required = true)
    private String email;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+91[6-9]\\d{9}$", message = "Phone number must be a valid Indian mobile number starting with +91")
    @Schema(description = "Phone number for verification", example = "+919876543210", required = true)
    private String phone;
    
    @Pattern(regexp = "^\\+91[6-9]\\d{9}$", message = "Alternate phone number must be a valid Indian mobile number starting with +91")
    @Schema(description = "Alternate phone number (optional)", example = "+919876543211", required = false)
    private String alternatePhone;
}
