package com.example.admin.dto;

import com.example.admin.dto.UserProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDeleteUserResponse {

    private boolean success;
    private String message;

    private UserProfileResponse data; // ✅ FULL DETAILS

    private LocalDateTime timestamp;
}