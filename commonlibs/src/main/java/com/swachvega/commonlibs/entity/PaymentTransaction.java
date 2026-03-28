package com.cyberlearnix.commonlibs.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Standalone payment intent record — created BEFORE an order exists.
 *
 * Flow:
 *   1. POST /api/payment/initiate  → creates PaymentTransaction (status = INITIATED)
 *   2. PayU processes payment       → webhook updates status to SUCCESS/FAILED
 *   3. POST /api/orders             → backend verifies txnId, creates order, links orderId here
 */
@Getter
@Setter
@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_pt_txn_id", columnList = "txnId", unique = true),
        @Index(name = "idx_pt_user_id", columnList = "userId"),
        @Index(name = "idx_pt_cart_id", columnList = "cartId"),
        @Index(name = "idx_pt_status", columnList = "status")
})
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Our generated txnId — format: SWACH_{timestamp}_{userId} */
    @Column(nullable = false, unique = true, length = 150)
    private String txnId;

    @Column(nullable = false)
    private Long cartId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2)")
    private BigDecimal amount;

    @Column(length = 255)
    private String productInfo;

    @Column(length = 100)
    private String customerName;

    @Column(length = 150)
    private String customerEmail;

    @Column(length = 20)
    private String customerPhone;

    /** Payment gateway used — PAYU, RAZORPAY, PHONEPE, CASHFREE, STRIPE etc. */
    @Column(length = 20)
    private String gateway = "PAYU";

    /** Preferred payment method — UPI, CREDIT_CARD, DEBIT_CARD, NET_BANKING, WALLET, EMI, COD */
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TxnStatus status = TxnStatus.INITIATED;

    /** PayU's mihpayid — set after PayU confirms the payment */
    @Column(length = 100)
    private String payuMihpayid;

    /** PayU payment mode (UPI, CC, DC, NB, etc.) */
    @Column(length = 30)
    private String payuPaymentMode;

    /** The orderId created after payment verification — null until order is placed */
    @Column
    private Long orderId;

    @Column
    private LocalDateTime expiresAt;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum TxnStatus {
        INITIATED,   // Payment intent created, awaiting PayU
        SUCCESS,     // PayU confirmed payment
        FAILED,      // PayU reported failure
        EXPIRED,     // Timed out — no callback received
        ORDER_CREATED // Order has been successfully created with this payment
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
