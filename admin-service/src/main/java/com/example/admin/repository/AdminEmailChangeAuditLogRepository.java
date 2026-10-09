package com.example.admin.repository;

import com.example.admin.entity.AdminEmailChangeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AdminEmailChangeAuditLogRepository extends JpaRepository<AdminEmailChangeAuditLog, UUID> {
}
