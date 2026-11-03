package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyCartResponse {
    private String cartId;
    private Integer totalCourses;
    private List<CourseInCartResponse> courses;
    private Double subtotal;
    private Double discount;
    private Double totalAmount;
    private String couponCode;

    public MyCartResponse(String cartId, Integer totalCourses, List<CourseInCartResponse> courses) {
        this.cartId = cartId;
        this.totalCourses = totalCourses;
        this.courses = courses;
    }
}
