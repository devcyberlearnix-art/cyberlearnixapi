package com.example.admin.dto;

import lombok.Data;

@Data
public class OrderReportDto {

    private long totalOrders;
    private long completedOrders;
    private long pendingOrders;
}
