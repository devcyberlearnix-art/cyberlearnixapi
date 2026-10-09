package com.example.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tracks the two-step email-change verification flow for Admin accounts.
 * OTP values are never persisted here — only the Redis session IDs that
 * reference the hashed OTPs stored by OtpService.
 */
@Entity
@Table(
    name = "admin_email_change_requests",
    indexes = {
        @Index(name = "idx_aecr_admin_id",      columnList = "admin_id"),
        @Index(name = "idx_aecr_admin_status",  columnList = "admin_id, status"),
        @Index(name = "idx_aecr_expiry_status", columnList = "expiry_time, status")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEmailChangeRequest {

    // ===== IDENTITY =====

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Foreign key to admins.id — stored as a plain UUID column for lightweight lookup. */
    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    // ===== EMAIL =====

    @Column(name = "new_email", nullable = false)
    private String newEmail;

    // ===== REDIS SESSION REFERENCES =====
    // These store the UUID session keys used to look up the hashed OTPs in Redis.
    // They are NOT the OTP values themselves.

    @Column(name = "old_otp_session_id")
    private String oldOtpSessionId;

    @Column(name = "new_otp_session_id")
    private String newOtpSessionId;

    // ===== VERIFICATION FLAGS =====

    @Builder.Default
    @Column(name = "old_email_verified", nullable = false)
    private boolean oldEmailVerified = false;

    @Builder.Default
    @Column(name = "new_email_verified", nullable = false)
    private boolean newEmailVerified = false;

    // ===== LIFECYCLE =====

    /** Request expires 10 minutes after creation (5-min OTP TTL + buffer). */
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "created_at", updatable = false, nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== ENUMS =====

    public enum Status {
        /** Both OTPs sent, waiting for verifications. */
        PENDING,
        /** Both emails verified; admin.email has been updated. */
        VERIFIED,
        /** Request exceeded its expiryTime without completing verification. */
        EXPIRED
    }

    // ===== HELPERS =====

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public boolean isCompleted() {
        return status == Status.VERIFIED || status == Status.EXPIRED;
    }
}
