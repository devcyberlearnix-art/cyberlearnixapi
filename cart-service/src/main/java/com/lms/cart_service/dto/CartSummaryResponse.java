package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryResponse {
    private Integer totalCourses;
    private Double subtotal;
    private Double discount;
    private Double totalAmount;
}
