package com.lms.paymentservice.dto;

import lombok.Data;

@Data
public class PaymentCallbackRequest {
    private String status;
    private String txnid;
    private String amount;
    private String productinfo;
    private String firstname;
    private String email;
    private String hash;
    private String mihpayid;
}
