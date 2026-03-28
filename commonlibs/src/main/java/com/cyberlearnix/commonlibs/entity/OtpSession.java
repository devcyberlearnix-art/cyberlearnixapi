package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "otp_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "session_id", updatable = false, nullable = false)
    private UUID sessionId;

    @Column(name = "identifier", nullable = false) // phone or email
    private String identifier;

    @Column(name = "otp_type", nullable = false) // LOGIN, REGISTRATION
    @Enumerated(EnumType.STRING)
    private OtpType otpType;

    @Column(name = "delivery_method", nullable = false) // SMS, EMAIL, WHATSAPP
    @Enumerated(EnumType.STRING)
    private DeliveryMethod deliveryMethod;

    @Column(name = "otp_hash", nullable = false) // Store hashed OTP for security
    private String otpHash;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "max_attempts", nullable = false)
    @Builder.Default
    private int maxAttempts = 3;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;

    @Column(name = "verified_at")
    private ZonedDateTime verifiedAt;

    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    // Helper method to check if OTP is expired
    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(expiresAt);
    }

    // Helper method to check if OTP is valid
    public boolean isValid() {
        return isActive && !isExpired() && !isVerified && attempts < maxAttempts;
    }

    // Helper method to check if attempts are exhausted
    public boolean isAttemptsExhausted() {
        return attempts >= maxAttempts;
    }

    @PrePersist
    public void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

    public enum OtpType {
        LOGIN, REGISTRATION
    }

    public enum DeliveryMethod {
        SMS, EMAIL, WHATSAPP
    }
}
