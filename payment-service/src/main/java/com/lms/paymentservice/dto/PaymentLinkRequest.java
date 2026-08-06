package com.lms.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentLinkRequest {
    private Double subAmount;
    private Boolean isPartialPaymentAllowed;
    private String description;
    private String source;
}

