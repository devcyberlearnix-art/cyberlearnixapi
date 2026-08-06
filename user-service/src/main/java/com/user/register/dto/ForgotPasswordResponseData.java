package com.user.register.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponseData {
    private String email;
    private int otpLength;
    private String otpSessionId;
    private LocalDateTime otpIssuedAt;
    private LocalDateTime otpExpiresAt;
    private Integer cooldownSeconds;
    private String message;
}