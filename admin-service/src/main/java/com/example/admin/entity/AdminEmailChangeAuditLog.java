package com.example.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit record written after a successful admin email change.
 * Captures who changed what, from where, and when.
 */
@Entity
@Table(
    name = "admin_email_change_audit_logs",
    indexes = {
        @Index(name = "idx_aecal_admin_id",    columnList = "admin_id"),
        @Index(name = "idx_aecal_created_at",  columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEmailChangeAuditLog {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(name = "old_email", nullable = false)
    private String oldEmail;

    @Column(name = "new_email", nullable = false)
    private String newEmail;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    /** SUCCESS or FAILURE. */
    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", updatable = false, nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
