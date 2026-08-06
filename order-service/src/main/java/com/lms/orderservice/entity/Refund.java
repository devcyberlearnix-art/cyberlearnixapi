package com.lms.orderservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String refundId;

    private String orderId;

    private Double amount;

    private String status;

    private LocalDateTime createdAt;
}
