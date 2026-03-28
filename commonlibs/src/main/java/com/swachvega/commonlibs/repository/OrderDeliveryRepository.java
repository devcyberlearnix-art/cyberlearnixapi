package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.OrderDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {

    /**
     * Find delivery by order ID
     */
    Optional<OrderDelivery> findByOrderId(Long orderId);

    /**
     * Find deliveries by status
     */
    List<OrderDelivery> findByStatusOrderByCreatedAtDesc(OrderDelivery.DeliveryStatus status);

    /**
     * Find deliveries by delivery partner
     */
    List<OrderDelivery> findByDeliveryPartnerNameOrderByCreatedAtDesc(String partnerName);

    /**
     * Find deliveries by tracking number
     */
    Optional<OrderDelivery> findByTrackingNumber(String trackingNumber);

    /**
     * Find deliveries scheduled for today
     */
    @Query("SELECT od FROM OrderDelivery od WHERE od.estimatedDeliveryDate >= :startOfDay " +
           "AND od.estimatedDeliveryDate < :startOfNextDay " +
           "AND od.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY') " +
           "ORDER BY od.estimatedDeliveryDate ASC")
    List<OrderDelivery> findDeliveriesToday(@Param("startOfDay") LocalDateTime startOfDay, 
                                          @Param("startOfNextDay") LocalDateTime startOfNextDay);

    /**
     * Find overdue deliveries
     */
    @Query("SELECT od FROM OrderDelivery od WHERE od.estimatedDeliveryDate < :currentTime " +
           "AND od.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY') " +
           "ORDER BY od.estimatedDeliveryDate ASC")
    List<OrderDelivery> findOverdueDeliveries(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find deliveries by city
     */
    List<OrderDelivery> findByCityOrderByEstimatedDeliveryDateAsc(String city);

    /**
     * Find deliveries by zip code
     */
    List<OrderDelivery> findByZipCodeOrderByEstimatedDeliveryDateAsc(String zipCode);

    /**
     * Find active deliveries for a partner
     */
    @Query("SELECT od FROM OrderDelivery od WHERE od.deliveryPartnerName = :partnerName " +
           "AND od.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'OUT_FOR_DELIVERY') " +
           "ORDER BY od.estimatedDeliveryDate ASC")
    List<OrderDelivery> findActiveDeliveriesForPartner(@Param("partnerName") String partnerName);

    /**
     * Find deliveries in a specific area (by lat/lng proximity)
     */
    @Query(value = "SELECT * FROM order_deliveries od WHERE " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(od.current_latitude)) * " +
           "cos(radians(od.current_longitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(od.current_latitude)))) <= :radiusKm " +
           "AND od.status IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY') " +
           "ORDER BY od.estimated_delivery_date ASC", 
           nativeQuery = true)
    List<OrderDelivery> findDeliveriesInArea(@Param("latitude") Double latitude,
                                           @Param("longitude") Double longitude,
                                           @Param("radiusKm") Double radiusKm);

    /**
     * Find deliveries by type
     */
    List<OrderDelivery> findByTypeOrderByEstimatedDeliveryDateAsc(OrderDelivery.DeliveryType type);

    /**
     * Find failed deliveries that can be retried
     */
    @Query("SELECT od FROM OrderDelivery od WHERE od.status = 'FAILED' " +
           "AND od.updatedAt > :retryThreshold " +
           "ORDER BY od.updatedAt ASC")
    List<OrderDelivery> findRetriableFailedDeliveries(@Param("retryThreshold") LocalDateTime retryThreshold);

    /**
     * Get delivery count by status
     */
    long countByStatus(OrderDelivery.DeliveryStatus status);

    /**
     * Calculate average delivery time in hours using native query
     */
    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (actual_delivery_date - actual_pickup_time))/3600) " +
           "FROM order_deliveries WHERE actual_delivery_date IS NOT NULL AND actual_pickup_time IS NOT NULL", 
           nativeQuery = true)
    Double getAverageDeliveryTimeInHours();

    /**
     * Find deliveries with location updates in last N minutes
     */
    @Query("SELECT od FROM OrderDelivery od WHERE od.lastLocationUpdate > :since " +
           "AND od.status IN ('IN_TRANSIT', 'OUT_FOR_DELIVERY') " +
           "ORDER BY od.lastLocationUpdate DESC")
    List<OrderDelivery> findDeliveriesWithRecentLocationUpdates(@Param("since") LocalDateTime since);

    /**
     * Check if tracking number exists
     */
    boolean existsByTrackingNumber(String trackingNumber);
}
