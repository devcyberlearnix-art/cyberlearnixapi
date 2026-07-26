package com.example.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.PrePersist;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    private UUID id;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    @Column(nullable = false, unique = true)
    private String email; // Indexed

    @Column(nullable = false)
    private String password; // BCrypt hashed

    @Column(nullable = false)
    private String role; // MAIN_ADMIN or SUB_ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AdminType adminType = AdminType.SUB_ADMIN;

    @Enumerated(EnumType.STRING)
    private AssignedService assignedService;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AdminApprovalStatus approvalStatus = AdminApprovalStatus.PENDING;

    private UUID approvedBy;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false; // default false

    // Plain text fields
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String alternateMobileNumber;

    // OTP for email verification
    @Column
    private String otp;

    @Column
    private LocalDateTime otpExpiry;

    // Track remaining OTP attempts (default 3)
    @Column(nullable = false)
    @Builder.Default
    private int otpAttempts = 3;

    // Block OTP input if exceeded attempts
    @Column(nullable = false)
    @Builder.Default
    private boolean otpBlocked = false;

    // Track last OTP sent time to control resend
    @Column
    private LocalDateTime lastOtpSentAt;
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
}