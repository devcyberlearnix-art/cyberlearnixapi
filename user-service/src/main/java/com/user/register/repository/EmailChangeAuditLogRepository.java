package com.user.register.repository;

import com.user.register.entity.EmailChangeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Append-only repository for email-change audit logs.
 * No update or delete methods should ever be called on this repository.
 */
public interface EmailChangeAuditLogRepository extends JpaRepository<EmailChangeAuditLog, Long> {

    /** Retrieve the full audit history for a user, ordered by most recent first. */
    List<EmailChangeAuditLog> findByUserIdOrderByChangedAtDesc(UUID userId);
}
