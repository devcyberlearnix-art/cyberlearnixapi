package com.lms.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private Long paymentId;
    private String txnId;
    private String hash;
    private String key;
    private String amount;
    private String firstName;
    private String email;
    private String phone;
    private String productInfo;
    private String courseId;
    private String instructorId;
    private String payerUserId;
    private String currency;
    private String surl;
    private String furl;
    private String actionUrl;
    private String payuActionUrl;
}
