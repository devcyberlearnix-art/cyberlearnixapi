package com.example.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDto {

    private String orderId;
    private String userId;
    private Double totalAmount;
    private String status;
    private LocalDateTime createdAt;
}