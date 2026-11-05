package com.lms.review.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserApiResponse {
    private boolean success;
    private String message;
    private UserResponse data;

    public String resolveDisplayName() {
        if (data != null) {
            if (data.getFullName() != null && !data.getFullName().isBlank()) {
                return data.getFullName();
            }
            if (data.getUsername() != null && !data.getUsername().isBlank()) {
                return data.getUsername();
            }
        }
        return null;
    }
}
