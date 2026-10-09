package com.user.register.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit record written once per email-change outcome (success or failure).
 * This table must NEVER be updated or deleted; it is append-only.
 */
@Entity
@Table(
    name = "email_change_audit_logs",
    indexes = {
        @Index(name = "idx_ecal_user_id",  columnList = "user_id"),
        @Index(name = "idx_ecal_changed_at", columnList = "changed_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailChangeAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who initiated the email change. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** Email address before the change. */
    @Column(name = "old_email", nullable = false)
    private String oldEmail;

    /** Email address after the change (or the attempted new email on failure). */
    @Column(name = "new_email", nullable = false)
    private String newEmail;

    /** Client IP address extracted from the HTTP request. */
    @Column(name = "ip_address")
    private String ipAddress;

    /** Raw User-Agent string from the HTTP request. */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /** Outcome: SUCCESS or FAILED. */
    @Column(name = "status", nullable = false)
    private String status;

    /** Optional detail about a failure (e.g. "OTP_EXPIRED", "MAX_ATTEMPTS"). */
    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "changed_at", updatable = false, nullable = false)
    @Builder.Default
    private LocalDateTime changedAt = LocalDateTime.now();

    // ===== CONSTANTS =====

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED  = "FAILED";
}
