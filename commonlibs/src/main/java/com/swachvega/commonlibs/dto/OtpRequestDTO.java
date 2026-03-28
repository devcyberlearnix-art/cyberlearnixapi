package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequestDTO {
    private String username;
    private String email;
    private String phoneNumber;
    private String deliveryMethod; // SMS, EMAIL, WHATSAPP
    private Boolean isRegistration; // true for registration, false for login
}
