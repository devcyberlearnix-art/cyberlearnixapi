package com.lms.paymentservice.entity;

/**
 * Tracks refund outcome after a refund is initiated (distinct from {@link PaymentStatus}).
 */
public enum RefundLifecycleStatus {
    PENDING,
    SUCCESS,
    FAILURE
}
