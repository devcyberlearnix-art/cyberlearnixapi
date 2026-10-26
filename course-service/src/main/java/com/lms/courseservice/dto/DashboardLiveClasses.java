package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardLiveClasses {
    private Integer totalClasses;
    private Integer attended;
    private Double attendancePercentage;
    private Long totalDurationSeconds;
}
