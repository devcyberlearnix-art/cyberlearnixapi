package com.lms.paymentservice.dto;

import lombok.Data;

/**
 * Refund initiation body (PayU mihpayid + optional client refund reference).
 * Example:
 * <pre>
 * {
 *   "txnId": "your_transaction_id",
 *   "refundRequestId": "refund_001",
 *   "amount": 100.00,
 *   "currency": "INR",
 *   "reason": "Customer requested cancellation"
 * }
 * </pre>
 */
@Data
public class RefundRequest {
    /**
     * Internal transaction ID (txnId).
     */
    private String txnId;
    /**
     * Client-supplied unique token for this refund attempt (sent to PayU as var2). Generated if omitted.
     */
    private String refundRequestId;
    private Double amount;
    private String currency;
    private String reason;
}
