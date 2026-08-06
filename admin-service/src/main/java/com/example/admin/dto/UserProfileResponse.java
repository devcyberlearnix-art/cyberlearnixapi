package com.example.admin.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserProfileResponse {
    private UUID userId;;
    private String firstName;
    private String lastName;
    private String email;
    private String mobile;
    private String dob;
    private String profilePhoto;
    private String city;
    private String state;
    private String country;
    private String preferredLanguage;
    private String organization;
    private String skills;
    private String fieldOfStudy;
    private String highestQualification;
    private String role;
    private String status;
    private String createdAt;
    private String updatedAt;
}