package com.user.register.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores password change OTP requests and the temporary new password hash.
 */
@Entity
@Table(
    name = "password_otps",
    indexes = {
        @Index(name = "idx_po_user_id", columnList = "user_id"),
        @Index(name = "idx_po_expiry", columnList = "expiry_time")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordOtp {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "otp_hash", nullable = false)
    private String otpHash;

    @Column(name = "new_password_hash", nullable = false)
    private String newPasswordHash;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING,
        VERIFIED,
        EXPIRED
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
