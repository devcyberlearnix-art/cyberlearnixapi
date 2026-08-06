package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class InstructorEarningsResponse {

    private UUID instructorId;
    private double totalRevenue;
    private double netEarnings;
    private double platformFee;
    private long totalEnrollments;
    private int totalCourses;

    private List<CourseEarningDTO> courseEarnings;
    private List<MonthlyEarningDTO> monthlyEarnings;
}
