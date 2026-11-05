package com.lms.review.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String username;
    private String email;

    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        String combined = (first + " " + last).trim();
        return combined.isBlank() ? null : combined;
    }

    public String resolveDisplayName() {
        String full = getFullName();
        if (full != null && !full.isBlank()) {
            return full;
        }
        if (username != null && !username.isBlank()) {
            return username;
        }
        if (email != null && !email.isBlank()) {
            return email;
        }
        return null;
    }
}
