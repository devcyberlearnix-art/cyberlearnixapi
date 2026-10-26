package com.lms.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates", uniqueConstraints = {
    @UniqueConstraint(name = "uk_certificate_number", columnNames = {"certificate_number"}),
    @UniqueConstraint(name = "uk_verification_code", columnNames = {"verification_code"})
},
indexes = {
    @Index(name = "idx_certificates_student_course", columnList = "student_id, course_id"),
    @Index(name = "idx_certificates_student_id", columnList = "student_id"),
    @Index(name = "idx_certificates_course_id", columnList = "course_id"),
    @Index(name = "idx_certificates_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "certificate_number", nullable = false, unique = true, length = 50)
    private String certificateNumber;

    @Column(name = "verification_code", nullable = false, unique = true, length = 100)
    private String verificationCode;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "student_name", nullable = false, length = 255)
    private String studentName;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_title", nullable = false, length = 500)
    private String courseTitle;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "certificate_url", length = 1000)
    private String certificateUrl;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CertificateStatus status = CertificateStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum CertificateStatus {
        ACTIVE,
        REVOKED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
