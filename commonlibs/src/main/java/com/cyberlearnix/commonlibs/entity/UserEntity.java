package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "username", unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column(name = "phone_number", length = 15, unique = true)
    private String phoneNumber;

    @Column(name = "alternate_number", length = 15)
    private String alternatePhoneNumber;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "referral_code")
    private String referralCode;

    @Column(name = "role", nullable = false)
    @Builder.Default
    private String role = "USER";

    @Column(name = "email_verified", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "phone_verified", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean phoneVerified = false;

    @Column(name = "accept_terms", nullable = false)
    @Builder.Default
    private boolean acceptTerms = false;

    @Column(name = "accept_privacy_policy", nullable = false)
    @Builder.Default
    private boolean acceptPrivacyPolicy = false;

    @Column(name = "subscribe_to_newsletter")
    @Builder.Default
    private boolean subscribeToNewsletter = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
}
