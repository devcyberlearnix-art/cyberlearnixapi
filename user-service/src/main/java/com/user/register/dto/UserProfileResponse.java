package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL) // ⬅ Omit null fields
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String firstName;
    private String lastName;
    private String name; // Computed: firstName + " " + lastName
    private String email;
    private String phone; // Alternative phone number field
    private String mobile;
    private String mobileNumber; // Alias for mobile (for frontend compatibility)
    private String dob;
    private String profilePhoto;
    private String coverPhoto; // Cover photo URL
    @Deprecated
    private String photoURL; // Deprecated: use profilePhoto instead
    @Deprecated
    private String avatar; // Deprecated: use profilePhoto instead
    private String city;
    private String state;
    private String country;
    private String countryCode;
    private String preferredLanguage;
    private String organization;
    private String skills;
    private String fieldOfStudy;
    private String highestQualification;
    private String gender;

    private UUID userId;
    private String id; // Alias for userId (for frontend compatibility)
    private String role; // single role as string
    private String userRole; // Alias for role (for frontend compatibility)
    private String status; // ACTIVE/SUSPENDED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
    private List<SessionDto> activeSessions;
}
