
package com.example.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminSingleUserResponse {

    private boolean success;
    private String message;
    private UserProfileResponse data;
    private String timestamp;
}