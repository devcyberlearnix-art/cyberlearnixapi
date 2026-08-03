package com.example.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewDto {

    private UUID id;
    private UUID userId;
    private Long courseId;

    private int rating;        // 1–5
    private String comment;

    private LocalDateTime createdAt;
}