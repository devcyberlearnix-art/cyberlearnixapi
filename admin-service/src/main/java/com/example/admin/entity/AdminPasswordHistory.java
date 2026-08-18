package com.example.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores historical password hashes for admins to prevent password reuse.
 */
@Entity
@Table(
    name = "admin_password_histories",
    indexes = {
        @Index(name = "idx_aph_admin_id", columnList = "admin_id"),
        @Index(name = "idx_aph_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPasswordHistory {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
