package com.user.register.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

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
    @GeneratedValue
    @UuidGenerator
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

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime submittedAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String rejectionReason;

    public enum ApplicationStatus {
        PENDING, APPROVED, REJECTED
    }
}
