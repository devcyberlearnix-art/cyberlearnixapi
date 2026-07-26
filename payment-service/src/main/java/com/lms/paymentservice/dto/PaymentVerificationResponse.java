package com.lms.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentVerificationResponse {
    private Long paymentId;
    private String txnId;
    private String status;
    private String message;
}
