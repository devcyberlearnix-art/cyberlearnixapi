package com.example.admin.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}

