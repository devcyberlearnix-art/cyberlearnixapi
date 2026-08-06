package com.lms.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String txnId;

    private Double amount;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    private String firstName;
    private String email;
    private String phone;
    private String productInfo;
    private String payuPaymentId;

    /**
     * Course service: course the learner paid for (instructor can list payments by course).
     */
    private String courseId;
    /**
     * Course service: instructor (course owner) user id.
     */
    private String instructorId;
    /**
     * Course service: learner user id who initiated payment.
     */
    private String payerUserId;

    /**
     * Amounts for this payment are in this currency (invoices and refunds assume INR for India).
     */
    private String currency;

    /**
     * Refund token sent to PayU as var2 (must be unique per refund attempt).
     */
    private String refundId;
    /**
     * PayU request id returned by refund/cancel API (request_id / txn_update_id).
     * Used for check_action_status_txnid.
     */
    private String refundRequestId;
    private Double refundAmount;
    private String refundReason;

    @Enumerated(EnumType.STRING)
    private RefundLifecycleStatus refundLifecycleStatus;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
