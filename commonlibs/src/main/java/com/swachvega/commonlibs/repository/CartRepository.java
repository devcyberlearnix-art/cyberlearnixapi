package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find active cart by user ID
     */
    @Query("SELECT c FROM Cart c WHERE c.user.userId = :userId AND c.status = :status")
    Optional<Cart> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    @Query("SELECT c FROM Cart c WHERE c.user.userId = :userId AND c.status = :status ORDER BY c.updatedAt DESC")
    List<Cart> findByUserIdAndStatusOrderByUpdatedAtDesc(@Param("userId") UUID userId, @Param("status") String status);

    /**
     * Find all carts by user ID
     */
    @Query("SELECT c FROM Cart c WHERE c.user.userId = :userId ORDER BY c.updatedAt DESC")
    List<Cart> findByUserIdOrderByUpdatedAtDesc(@Param("userId") UUID userId);

    /**
     * Find expired carts
     */
    @Query("SELECT c FROM Cart c WHERE c.expiresAt < :currentTime AND c.status = 'ACTIVE'")
    List<Cart> findExpiredCarts(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count active carts by user ID
     */
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user.userId = :userId AND c.status = :status")
    Long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);

    /**
     * Find abandoned carts (not updated for specific time)
     */
    @Query("SELECT c FROM Cart c WHERE c.updatedAt < :cutoffTime AND c.status = 'ACTIVE'")
    List<Cart> findAbandonedCarts(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Delete expired carts
     */
    void deleteByExpiresAtBeforeAndStatus(LocalDateTime expiredBefore, String status);

    /**
     * Check if user has any active cart
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cart c WHERE c.user.userId = :userId AND c.status = :status")
    boolean existsByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);
}
