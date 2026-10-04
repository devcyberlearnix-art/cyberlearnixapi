package com.lms.wishlist_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistCreateRequest {
    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotBlank(message = "Instructor ID is required")
    private String instructorId;
}
