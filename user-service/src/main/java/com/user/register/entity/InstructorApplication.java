package com.user.register.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instructor_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

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

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    private String subject;
    private String specialization;
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String phone;
    private String linkedinUrl;
    private String websiteUrl;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @Column(columnDefinition = "TEXT")
    private String requestedInfo;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String rejectionReason;

    public enum ApplicationStatus {
        DRAFT, PENDING, UNDER_REVIEW, NEED_MORE_INFO, APPROVED, REJECTED, WITHDRAWN
    }
}
