package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@Entity
@Table(name = "order_payments", indexes = {
        @Index(name = "idx_payment_order_id", columnList = "orderId"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_method", columnList = "paymentMethod")
})
public class OrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(length = 100)
    private String transactionId; // Our generated txnId (payu_orderId_timestamp)

    @Column(length = 100)
    private String paymentGateway; // PayU, Razorpay, Stripe, etc.

    @Column(length = 50)
    private String paymentReference; // Internal payment reference

    @Column(name = "payu_mihpayid", length = 100)
    private String payuMihpayid; // PayU's own transaction reference returned in callback

    @Column(name = "payu_refund_id", length = 100)
    private String payuRefundId; // Our refund ID sent to PayU (ref_<orderId>_<uuid12>)

    @Column(length = 500)
    private String paymentDetails; // JSON or additional payment details

    @Column
    private LocalDateTime paymentDate;

    @Column
    private LocalDateTime refundDate;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 500)
    private String refundReason;

    /** Number of times payment initiation has been attempted for this order (used to enforce max retries) */
    @Column(name = "payment_attempt_count", nullable = false)
    private int paymentAttemptCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // One-to-one relationship with Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", insertable = false, updatable = false)
    private Order order;

    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        CREDIT_CARD,
        DEBIT_CARD,
        UPI,
        NET_BANKING,
        WALLET,
        BANK_TRANSFER,
        PAYU_ONLINE   // Fallback when PayU returns an unrecognised payment mode
    }

    public enum PaymentStatus {
        PENDING,        // Payment not yet processed
        PROCESSING,     // Payment being processed
        COMPLETED,      // Payment successful
        FAILED,         // Payment failed
        CANCELLED,      // Payment cancelled
        REFUNDED,       // Payment refunded
        PARTIAL_REFUND  // Partial refund processed
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    public void markCompleted(String transactionId, String gateway) {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
            throw new IllegalStateException("Payment can only be completed from PENDING or PROCESSING status");
        }
        this.status = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.paymentGateway = gateway;
        this.paidAmount = this.amount != null ? this.amount : BigDecimal.ZERO;
        this.paymentDate = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void processRefund(BigDecimal refundAmount, String reason) {
        if (status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Can only refund completed payments");
        }
        
        BigDecimal pending = getPendingRefundAmount();
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0 || refundAmount.compareTo(pending) > 0) {
            throw new IllegalArgumentException("Invalid refund amount");
        }
        
        this.refundedAmount = this.refundedAmount.add(refundAmount).setScale(2, RoundingMode.HALF_UP);
        this.refundReason = reason;
        this.refundDate = LocalDateTime.now();
        
        if (this.refundedAmount.compareTo(this.paidAmount) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIAL_REFUND;
        }
    }

    public BigDecimal getPendingRefundAmount() {
        BigDecimal paid = this.paidAmount != null ? this.paidAmount : BigDecimal.ZERO;
        BigDecimal refunded = this.refundedAmount != null ? this.refundedAmount : BigDecimal.ZERO;
        return paid.subtract(refunded).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isFullyPaid() {
        return status == PaymentStatus.COMPLETED && paidAmount != null && amount != null && paidAmount.compareTo(amount) == 0;
    }

    public boolean canBeRefunded() {
        return status == PaymentStatus.COMPLETED || status == PaymentStatus.PARTIAL_REFUND;
    }
}
