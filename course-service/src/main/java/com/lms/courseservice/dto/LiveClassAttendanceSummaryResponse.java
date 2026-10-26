package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveClassAttendanceSummaryResponse {

    private Long liveClassId;
    private Long totalEnrolledStudents;
    private Long totalJoined;
    private Long totalAttended;
    private Long totalCurrentlyJoined;
    private Long totalAbsent;
    private Double attendancePercentage;
    private Long averageDurationSeconds;
}
