package com.example.admin.repository;

import com.example.admin.entity.AdminPasswordChangeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdminPasswordChangeAuditLogRepository extends JpaRepository<AdminPasswordChangeAuditLog, UUID> {
}
