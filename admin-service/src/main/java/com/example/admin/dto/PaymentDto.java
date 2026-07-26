package com.example.admin.dto;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentDto {

    private Long id;
    private String txnId;
    private String userId;

    private Double amount;
    private String status;     // SUCCESS / FAILED / REFUNDED
    private String currency;
    private String method;     // CARD / UPI / NETBANKING

    private LocalDateTime createdAt;
}
