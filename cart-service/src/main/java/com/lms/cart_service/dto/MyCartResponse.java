package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyCartResponse {
    private String cartId;
    private Integer totalCourses;
    private List<CourseInCartResponse> courses;
}
