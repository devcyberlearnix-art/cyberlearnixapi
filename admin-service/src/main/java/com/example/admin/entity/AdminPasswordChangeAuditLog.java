package com.example.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Audit log recording admin password changes.
 */
@Entity
@Table(
    name = "admin_password_change_audit_logs",
    indexes = {
        @Index(name = "idx_apcal_admin_id", columnList = "admin_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPasswordChangeAuditLog {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "device")
    private String device;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
