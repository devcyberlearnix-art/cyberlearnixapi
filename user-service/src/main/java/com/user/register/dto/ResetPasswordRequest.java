package com.user.register.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
        private String resetToken;   // ✅ change to accessToken
        private String newPassword;
        private String confirmPassword;
    }
