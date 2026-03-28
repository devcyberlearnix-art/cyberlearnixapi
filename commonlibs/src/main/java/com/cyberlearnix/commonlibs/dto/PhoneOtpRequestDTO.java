package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for sending OTP to phone number")
public class PhoneOtpRequestDTO {
    
    @Schema(
        description = "10-digit Indian mobile number", 
        example = "9876543210",
        required = true
    )
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid Indian mobile number format")
    private String phone;
    
    @Schema(
        description = "OTP delivery method", 
        example = "SMS",
        allowableValues = {"SMS", "WHATSAPP"},
        defaultValue = "SMS"
    )
    private String deliveryMethod; // SMS, WHATSAPP
}
