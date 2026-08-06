package com.lms.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundResponse {
    /**
     * Normalized outcome: PENDING, SUCCESS, or FAILURE.
     */
    private String status;
    private String message;
    private Double amount;
    private String currency;
    /** Internal transaction ID. */
    private String txnId;
    /** Token sent to gateway (var2). */
    private String refundId;
    /** PayU request id for status polling when returned by gateway. */
    private String gatewayRequestId;
    private String reason;
}
