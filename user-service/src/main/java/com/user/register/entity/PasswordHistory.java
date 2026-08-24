package com.user.register.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores historical password hashes for users to prevent password reuse.
 */
@Entity
@Table(
    name = "password_histories",
    indexes = {
        @Index(name = "idx_ph_user_id", columnList = "user_id"),
        @Index(name = "idx_ph_created_at", columnList = "created_at")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordHistory {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
