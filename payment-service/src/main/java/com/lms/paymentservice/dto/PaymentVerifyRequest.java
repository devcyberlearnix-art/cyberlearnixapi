package com.lms.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentVerifyRequest {
    private String txnId;
}
