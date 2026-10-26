package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseInCartResponse {
    private String courseId;
    private String title;
    private Double price;
    private Integer quantity;

    public CourseInCartResponse(String courseId, String title, Double price) {
        this.courseId = courseId;
        this.title = title;
        this.price = price;
        this.quantity = 1;
    }
}
