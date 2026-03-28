package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find order by ID with all associations eagerly fetched to avoid LazyInitializationException
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "LEFT JOIN FETCH o.delivery " +
           "LEFT JOIN FETCH o.payment " +
           "WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);

    /**
     * Find order by order number with all associations eagerly fetched
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "LEFT JOIN FETCH o.delivery " +
           "LEFT JOIN FETCH o.payment " +
           "WHERE o.orderNumber = :orderNumber")
    Optional<Order> findByOrderNumberWithDetails(@Param("orderNumber") String orderNumber);

    /**
     * Find orders by user ID ordered by order date descending
     * Optimized with JOIN FETCH to avoid N+1 queries for orderItems, delivery, payment
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "LEFT JOIN FETCH o.delivery " +
           "LEFT JOIN FETCH o.payment " +
           "WHERE o.userId = :userId " +
           "ORDER BY o.orderDate DESC")
    List<Order> findByUserIdOrderByOrderDateDesc(@Param("userId") UUID userId);

    /**
     * Find orders by store ID ordered by order date descending
     * Optimized with JOIN FETCH to avoid N+1 queries
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "LEFT JOIN FETCH o.delivery " +
           "LEFT JOIN FETCH o.payment " +
           "WHERE o.storeId = :storeId " +
           "ORDER BY o.orderDate DESC")
    List<Order> findByStoreIdOrderByOrderDateDesc(@Param("storeId") Long storeId);

    /**
     * Find orders by status
     */
    List<Order> findByStatusOrderByOrderDateDesc(Order.OrderStatus status);

    /**
     * Find orders by user and status
     * Optimized with JOIN FETCH to avoid N+1 queries
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "LEFT JOIN FETCH o.delivery " +
           "LEFT JOIN FETCH o.payment " +
           "WHERE o.userId = :userId AND o.status = :status " +
           "ORDER BY o.orderDate DESC")
    List<Order> findByUserIdAndStatusOrderByOrderDateDesc(@Param("userId") UUID userId, @Param("status") Order.OrderStatus status);

    /**
     * Find orders by store and status
     * Optimized with JOIN FETCH to avoid N+1 queries
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderItems " +
           "LEFT JOIN FETCH o.delivery " +
           "LEFT JOIN FETCH o.payment " +
           "WHERE o.storeId = :storeId AND o.status = :status " +
           "ORDER BY o.orderDate DESC")
    List<Order> findByStoreIdAndStatusOrderByOrderDateDesc(@Param("storeId") Long storeId, @Param("status") Order.OrderStatus status);

    /**
     * Find orders by cart ID
     */
    Optional<Order> findByCartId(Long cartId);

    /**
     * Find orders created within date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent orders for a user (last 30 days)
     */
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.orderDate >= :since ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersByUser(@Param("userId") UUID userId, @Param("since") LocalDateTime since);

    /**
     * Find active orders (orders that are not delivered, cancelled, or refunded)
     */
    @Query("SELECT o FROM Order o WHERE o.status NOT IN ('DELIVERED', 'CANCELLED', 'REFUNDED') ORDER BY o.orderDate DESC")
    List<Order> findActiveOrders();

    /**
     * Find orders pending confirmation
     */
    List<Order> findByStatusAndOrderDateBeforeOrderByOrderDateAsc(Order.OrderStatus status, LocalDateTime before);

    /**
     * Get total orders count for a user
     */
    long countByUserId(UUID userId);

    /**
     * Get total orders count for a store
     */
    long countByStoreId(Long storeId);

    /**
     * Calculate total revenue for a store
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.storeId = :storeId AND o.status = 'DELIVERED'")
    Double calculateStoreRevenue(@Param("storeId") Long storeId);

    /**
     * Calculate total revenue for a store within date range
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.storeId = :storeId AND o.status = 'DELIVERED' " +
           "AND o.deliveredAt BETWEEN :startDate AND :endDate")
    Double calculateStoreRevenueByDateRange(@Param("storeId") Long storeId, 
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Find orders requiring attention (pending too long)
     */
    @Query("SELECT o FROM Order o WHERE " +
           "(o.status = 'PENDING' AND o.orderDate < :pendingThreshold) OR " +
           "(o.status = 'CONFIRMED' AND o.confirmedAt < :confirmedThreshold) OR " +
           "(o.status = 'PACKED' AND o.packedAt < :packedThreshold)")
    List<Order> findOrdersRequiringAttention(@Param("pendingThreshold") LocalDateTime pendingThreshold,
                                           @Param("confirmedThreshold") LocalDateTime confirmedThreshold,
                                           @Param("packedThreshold") LocalDateTime packedThreshold);

    /**
     * Check if user exists based on orders
     */
    boolean existsByUserId(UUID userId);

    /**
     * Check if store has any orders
     */
    boolean existsByStoreId(Long storeId);
}
