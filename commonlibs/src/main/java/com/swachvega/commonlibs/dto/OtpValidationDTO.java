package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpValidationDTO {
    private String username;
    private String email;
    private String phoneNumber;
    private String otp;
    private String otpSessionId;
}
