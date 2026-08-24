package com.user.register.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String action;

    private String ipAddress;
    private String device; // <-- Add this field
    private String status; // <-- Add this field for SUCCESS/FAILURE

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}