package com.lms.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentLinkCreateRequest {
    private String merchantId;
    private String accessToken;
    private PaymentLinkRequest payload;
}

