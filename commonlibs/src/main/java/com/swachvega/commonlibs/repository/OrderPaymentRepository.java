package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.OrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {

    /**
     * Find payment by order ID
     */
    Optional<OrderPayment> findByOrderId(Long orderId);

    /**
     * Find payment by transaction ID
     */
    Optional<OrderPayment> findByTransactionId(String transactionId);

    /**
     * Find payment by payment reference
     */
    Optional<OrderPayment> findByPaymentReference(String paymentReference);

    /**
     * Find payments by status
     */
    List<OrderPayment> findByStatusOrderByCreatedAtDesc(OrderPayment.PaymentStatus status);

    /**
     * Find payments by payment method
     */
    List<OrderPayment> findByPaymentMethodOrderByCreatedAtDesc(OrderPayment.PaymentMethod paymentMethod);

    /**
     * Find payments by payment gateway
     */
    List<OrderPayment> findByPaymentGatewayOrderByCreatedAtDesc(String paymentGateway);

    /**
     * Find failed payments that can be retried
     */
    @Query("SELECT op FROM OrderPayment op WHERE op.status = 'FAILED' " +
           "AND op.updatedAt > :retryThreshold " +
           "ORDER BY op.updatedAt ASC")
    List<OrderPayment> findRetriableFailedPayments(@Param("retryThreshold") LocalDateTime retryThreshold);

    /**
     * Find pending payments older than threshold
     */
    @Query("SELECT op FROM OrderPayment op WHERE op.status = 'PENDING' " +
           "AND op.createdAt < :threshold " +
           "ORDER BY op.createdAt ASC")
    List<OrderPayment> findPendingPaymentsOlderThan(@Param("threshold") LocalDateTime threshold);

    /**
     * Find PROCESSING payments older than threshold (abandoned payments)
     */
    @Query("SELECT op FROM OrderPayment op WHERE op.status = 'PROCESSING' " +
           "AND op.updatedAt < :threshold " +
           "ORDER BY op.updatedAt ASC")
    List<OrderPayment> findProcessingPaymentsOlderThan(@Param("threshold") LocalDateTime threshold);

    /**
     * Find payments within date range
     */
    @Query("SELECT op FROM OrderPayment op WHERE op.paymentDate BETWEEN :startDate AND :endDate " +
           "AND op.status = 'COMPLETED' " +
           "ORDER BY op.paymentDate DESC")
    List<OrderPayment> findPaymentsInDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total revenue by payment method
     */
    @Query("SELECT op.paymentMethod, COALESCE(SUM(op.paidAmount), 0) " +
           "FROM OrderPayment op WHERE op.status = 'COMPLETED' " +
           "GROUP BY op.paymentMethod")
    List<Object[]> calculateRevenueByPaymentMethod();

    /**
     * Calculate total revenue in date range
     */
    @Query("SELECT COALESCE(SUM(op.paidAmount), 0) FROM OrderPayment op " +
           "WHERE op.status = 'COMPLETED' AND op.paymentDate BETWEEN :startDate AND :endDate")
    Double calculateRevenueInDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Calculate total refunds in date range
     */
    @Query("SELECT COALESCE(SUM(op.refundedAmount), 0) FROM OrderPayment op " +
           "WHERE (op.status = 'REFUNDED' OR op.status = 'PARTIAL_REFUND') " +
           "AND op.refundDate BETWEEN :startDate AND :endDate")
    Double calculateRefundsInDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find payments requiring refund
     */
    @Query("SELECT op FROM OrderPayment op WHERE op.status = 'COMPLETED' " +
           "AND EXISTS (SELECT 1 FROM Order o WHERE o.orderId = op.orderId AND o.status = 'CANCELLED')")
    List<OrderPayment> findPaymentsRequiringRefund();

    /**
     * Get payment count by status
     */
    long countByStatus(OrderPayment.PaymentStatus status);

    /**
     * Get payment count by method
     */
    long countByPaymentMethod(OrderPayment.PaymentMethod paymentMethod);

    /**
     * Find COD payments
     */
    @Query("SELECT op FROM OrderPayment op WHERE op.paymentMethod = 'CASH_ON_DELIVERY' ORDER BY op.createdAt DESC")
    List<OrderPayment> findCashOnDeliveryPayments();

    /**
     * Calculate success rate by payment method
     */
    @Query("SELECT op.paymentMethod, " +
           "COUNT(*) as totalTransactions, " +
           "SUM(CASE WHEN op.status = 'COMPLETED' THEN 1 ELSE 0 END) as successfulTransactions " +
           "FROM OrderPayment op " +
           "GROUP BY op.paymentMethod")
    List<Object[]> calculateSuccessRateByPaymentMethod();

    /**
     * Check if transaction ID exists
     */
    boolean existsByTransactionId(String transactionId);

    /**
     * Check if payment reference exists
     */
    boolean existsByPaymentReference(String paymentReference);
}
