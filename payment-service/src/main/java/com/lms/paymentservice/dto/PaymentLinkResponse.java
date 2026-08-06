package com.lms.paymentservice.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PaymentLinkResponse {
    private Integer status;
    private String message;
    private Map<String, Object> result;
    private String errorCode;
    private String guid;
}

