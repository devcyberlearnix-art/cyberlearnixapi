package com.lms.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRequest {
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotBlank(message = "Instructor ID is required")
    private String instructorId;

    // These replace the "Course Service" lookup
    private String courseName;
    private Double price;
}
