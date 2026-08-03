package com.example.admin.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpResponse {
    private boolean success;
    private String message;
    private String timestamp;
    private AdminInfo data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminInfo {
        private UUID id;
        private String email;
        private String role;
        private String firstName;
        private String lastName;
        private String mobileNumber;
        private String alternateMobileNumber;
    }
}
