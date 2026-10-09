package com.example.admin.repository;

import com.example.admin.entity.AdminPasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AdminPasswordOtpRepository extends JpaRepository<AdminPasswordOtp, UUID> {

    Optional<AdminPasswordOtp> findByIdAndAdminId(UUID id, UUID adminId);

    @Modifying
    @Query("UPDATE AdminPasswordOtp apo SET apo.status = 'EXPIRED' WHERE apo.adminId = :adminId AND apo.status = 'PENDING'")
    int expirePendingOtpSessionsForAdmin(@Param("adminId") UUID adminId);
}
