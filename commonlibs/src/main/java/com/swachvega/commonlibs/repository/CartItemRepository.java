package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all items by cart ID
     */
    List<CartItem> findByCartIdOrderByAddedAtDesc(Long cartId);

    /**
     * Find specific item in cart
     */
    Optional<CartItem> findByCartIdAndProductIdAndStoreId(Long cartId, Long productId, Long storeId);

    /**
     * Count items in cart
     */
    Long countByCartId(Long cartId);

    /**
     * Get total weight for cart
     */
    @Query("SELECT COALESCE(SUM(ci.totalWeight), 0.0) FROM CartItem ci WHERE ci.cartId = :cartId")
    Double getTotalWeightByCartId(@Param("cartId") Long cartId);

    /**
     * Get total amount for cart
     */
    @Query("SELECT COALESCE(SUM(ci.finalPrice), 0.0) FROM CartItem ci WHERE ci.cartId = :cartId")
    Double getTotalAmountByCartId(@Param("cartId") Long cartId);

    /**
     * Delete all items by cart ID
     */
    void deleteByCartId(Long cartId);

    /**
     * Find items by product ID (for inventory management)
     */
    List<CartItem> findByProductId(Long productId);

    /**
     * Find items by store ID
     */
    List<CartItem> findByStoreId(Long storeId);

    /**
     * Check if product exists in any cart
     */
    boolean existsByProductIdAndStoreId(Long productId, Long storeId);
}
