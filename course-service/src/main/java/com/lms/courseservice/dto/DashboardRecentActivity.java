package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRecentActivity {
    private String activityType;
    private String description;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime timestamp;
}
