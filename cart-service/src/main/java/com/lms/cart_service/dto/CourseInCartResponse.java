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
}
