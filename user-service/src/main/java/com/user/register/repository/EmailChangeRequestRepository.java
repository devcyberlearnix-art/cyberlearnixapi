package com.user.register.repository;

import com.user.register.entity.EmailChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailChangeRequestRepository extends JpaRepository<EmailChangeRequest, UUID> {

    /**
     * Fetch a request only if it belongs to the authenticated user (prevents IDOR).
     */
    Optional<EmailChangeRequest> findByIdAndUserId(UUID id, UUID userId);

    /**
     * Find all PENDING requests for a user (there should never be more than one,
     * but this is used for cleanup before creating a new one).
     */
    List<EmailChangeRequest> findByUserIdAndStatus(UUID userId, EmailChangeRequest.Status status);

    /**
     * Check whether the requested new email is already pending in another (different) request.
     * Useful as a secondary uniqueness guard alongside the main users table check.
     */
    boolean existsByNewEmailAndStatus(String newEmail, EmailChangeRequest.Status status);

    /**
     * Bulk-expire all PENDING requests whose expiryTime has passed.
     * Called periodically or on-demand before creating a new request.
     */
    @Modifying
    @Query("""
        UPDATE EmailChangeRequest e
           SET e.status = 'EXPIRED'
         WHERE e.status = 'PENDING'
           AND e.expiryTime < :now
        """)
    int expireStaleRequests(@Param("now") LocalDateTime now);

    /**
     * Expire any existing PENDING requests for a specific user.
     * Called before creating a new request so only one is active at a time.
     */
    @Modifying
    @Query("""
        UPDATE EmailChangeRequest e
           SET e.status = 'EXPIRED'
         WHERE e.userId = :userId
           AND e.status = 'PENDING'
        """)
    int expirePendingRequestsForUser(@Param("userId") UUID userId);
}
