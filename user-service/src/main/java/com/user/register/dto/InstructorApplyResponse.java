package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstructorApplyResponse {

    private UUID userId;
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
    private String appliedRole;
    private String applicationStatus;

    private UUID applicationId;
    private String resumePath;
    private String educationalCertificatesPath;
    private String governmentIdProofPath;
    private String experienceLetterPath;
    private String internshipCertificatePath;
    private String skillCertificatesPath;
    private String portfolioPath;
    private String demoLecturePptPath;
    private String demoLectureRecordingPath;
    private String projectsPath;
    private String passportPhotoPath;
    private String bankDetailsPath;
    private String panDocumentPath;
    private String applicationFormPath;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;
    private String panNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
