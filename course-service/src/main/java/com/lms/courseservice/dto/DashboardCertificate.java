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
public class DashboardCertificate {
    private Long certificateId;
    private Long courseId;
    private String courseTitle;
    private String certificateNumber;
    private LocalDateTime issuedAt;
}
