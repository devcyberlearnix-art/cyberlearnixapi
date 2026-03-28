package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRegistrationDTO {
    private String phoneNumber;
    private String name; // Full name
    private String email; // Email address
    private String tempToken; // Temporary token from OTP validation
}
