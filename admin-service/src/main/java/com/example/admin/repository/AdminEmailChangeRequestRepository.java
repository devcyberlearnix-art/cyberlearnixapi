package com.example.admin.repository;

import com.example.admin.entity.AdminEmailChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AdminEmailChangeRequestRepository extends JpaRepository<AdminEmailChangeRequest, UUID> {

    Optional<AdminEmailChangeRequest> findByIdAndAdminId(UUID id, UUID adminId);

    boolean existsByNewEmailAndStatus(String newEmail, AdminEmailChangeRequest.Status status);

    /**
     * Marks all PENDING requests for the given admin as EXPIRED.
     * Used when a new request is initiated to clean up stale sessions.
     */
    @Modifying
    @Query("UPDATE AdminEmailChangeRequest r SET r.status = 'EXPIRED' " +
           "WHERE r.adminId = :adminId AND r.status = 'PENDING'")
    int expirePendingRequestsForAdmin(@Param("adminId") UUID adminId);
}
