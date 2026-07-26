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

    private boolean success;
    private UserProfileData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfileData {
        private String firstName;
        private String lastName;
        private UUID userId;
    }

    public String resolveDisplayName() {
        if (data == null) {
            return null;
        }
        String firstName = data.getFirstName() != null ? data.getFirstName().trim() : "";
        String lastName = data.getLastName() != null ? data.getLastName().trim() : "";
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? null : fullName;
    }
}
