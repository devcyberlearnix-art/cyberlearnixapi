package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find items by order ID
     */
    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    /**
     * Find items by product ID
     */
    List<OrderItem> findByProductIdOrderByCreatedAtDesc(Long productId);

    /**
     * Find items by store ID
     */
    List<OrderItem> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    /**
     * Calculate total quantity ordered for a product
     */
    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.productId = :productId")
    Long getTotalQuantityOrderedForProduct(@Param("productId") Long productId);

    /**
     * Calculate total revenue for a product
     */
    @Query("SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi WHERE oi.productId = :productId")
    Double getTotalRevenueForProduct(@Param("productId") Long productId);

    /**
     * Find most popular products by quantity
     */
    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findMostPopularProductsByQuantity();

    /**
     * Find most revenue generating products
     */
    @Query("SELECT oi.productId, oi.productName, SUM(oi.totalPrice) as totalRevenue " +
           "FROM OrderItem oi GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalRevenue DESC")
    List<Object[]> findMostRevenueGeneratingProducts();

    /**
     * Find popular products for a specific store
     */
    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalQuantity " +
           "FROM OrderItem oi WHERE oi.storeId = :storeId " +
           "GROUP BY oi.productId, oi.productName " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findPopularProductsForStore(@Param("storeId") Long storeId);

    /**
     * Find products ordered within date range
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY oi.createdAt DESC")
    List<OrderItem> findProductsOrderedInDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Get order item count for an order
     */
    long countByOrderId(Long orderId);

    /**
     * Calculate total weight for an order
     */
    @Query("SELECT COALESCE(SUM(oi.totalWeight), 0) FROM OrderItem oi WHERE oi.orderId = :orderId")
    Double getTotalWeightForOrder(@Param("orderId") Long orderId);

    /**
     * Calculate total price for an order
     */
    @Query("SELECT COALESCE(SUM(oi.totalPrice), 0) FROM OrderItem oi WHERE oi.orderId = :orderId")
    Double getTotalPriceForOrder(@Param("orderId") Long orderId);

    /**
     * Check if product has been ordered
     */
    boolean existsByProductId(Long productId);

    /**
     * Find items with special instructions
     */
    List<OrderItem> findBySpecialInstructionsIsNotNullOrderByCreatedAtDesc();

    /**
     * Find user's frequently bought products with the store they bought from most
     * Returns: productId, storeId, COUNT(orders) - one row per product showing the store where user bought it most
     */
    @Query(value = "SELECT oi.product_id, oi.store_id, COUNT(DISTINCT o.order_id) as order_count " +
           "FROM order_items oi " +
           "JOIN orders o ON oi.order_id = o.order_id " +
           "WHERE o.user_id = CAST(:userId AS uuid) AND o.status IN ('DELIVERED', 'COMPLETED') " +
           "GROUP BY oi.product_id, oi.store_id " +
           "ORDER BY oi.product_id, order_count DESC", 
           nativeQuery = true)
    List<Object[]> findUserFrequentlyBoughtProductsWithStore(@Param("userId") String userId);
    
    /**
     * Find most popular products across ALL users with their most popular store
     * Returns: productId, storeId, total_order_count - products ordered most frequently with the store that had most orders
     * Used for recommendations when user has no order history
     */
    @Query(value = "SELECT oi.product_id, oi.store_id, COUNT(DISTINCT o.order_id) as order_count " +
           "FROM order_items oi " +
           "JOIN orders o ON oi.order_id = o.order_id " +
           "WHERE o.status IN ('DELIVERED', 'COMPLETED') " +
           "GROUP BY oi.product_id, oi.store_id " +
           "ORDER BY order_count DESC " +
           "LIMIT 50", 
           nativeQuery = true)
    List<Object[]> findMostPopularProductsWithStore();
}