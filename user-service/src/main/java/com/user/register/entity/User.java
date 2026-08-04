package com.user.register.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private String firstName;
    private String lastName;
    @Column
    private String email;
    private String password;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String confirmPassword;

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

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    private Role appliedRole;

    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus applicationStatus;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.STUDENT;

    @Builder.Default
    private Boolean isInstructorApproved = false;

    private LocalDateTime lastLogin;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    private String ipAddress;
    private String device;
    private String browser;
    private String os;

    @Column
    @JsonProperty("mobileNumber")
    private String mobile;

    @Column
    @Builder.Default
    private String mobileHash = "";

    private String userAgent;

    @Column(name = "provider")
    private String provider;

    private String providerId;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column
    private LocalDateTime lockedUntil; // null means not locked

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    private String countryCode;

    // ===== LIFECYCLE =====
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void setRefreshToken(String refreshToken) {
        // implement if needed
    }

    // ===== ENUMS =====
    public enum Status {
        PENDING_VERIFICATION, ACTIVE, LOCKED, SUSPENDED,
        SOCIAL_LOGIN, DELETED, REJECTED
    }

    public enum Role {
        STUDENT, INSTRUCTOR, MAIN_ADMIN, SUB_ADMIN
    }

    public enum ApplicationStatus {
        PENDING, PENDING_VERIFICATION, APPROVED, REJECTED, SOCIAL_LOGIN
    }

    // ===== BUSINESS LOGIC =====
    public String getEffectiveRole() {
        if (this.role == Role.INSTRUCTOR ||
                (this.appliedRole == Role.INSTRUCTOR && Boolean.TRUE.equals(this.isInstructorApproved))) {
            return Boolean.TRUE.equals(this.isInstructorApproved) ? "INSTRUCTOR" : "STUDENT";
        }
        return this.role.name();
    }
}