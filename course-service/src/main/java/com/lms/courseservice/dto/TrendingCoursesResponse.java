package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingCoursesResponse {
    private Boolean success;
    private String message;
    private TrendingResponseData data;
    private String timestamp;
}