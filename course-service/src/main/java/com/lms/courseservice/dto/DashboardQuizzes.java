package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardQuizzes {
    private Integer totalAttempts;
    private Double averageScore;
    private Integer passed;
    private Integer failed;
}
