package com.user.register.repository;

import com.user.register.entity.PasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PasswordOtpRepository extends JpaRepository<PasswordOtp, UUID> {

    Optional<PasswordOtp> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE PasswordOtp po SET po.status = 'EXPIRED' WHERE po.userId = :userId AND po.status = 'PENDING'")
    int expirePendingOtpSessionsForUser(@Param("userId") UUID userId);
}
