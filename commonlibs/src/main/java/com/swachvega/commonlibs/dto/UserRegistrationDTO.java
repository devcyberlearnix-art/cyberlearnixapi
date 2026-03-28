package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDTO {
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String referralCode;
    private String otp;
    private String otpSessionId;
    private boolean acceptTerms;
    private boolean acceptPrivacyPolicy;
    private boolean subscribeToNewsletter;
}
