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
public class CertificateResponse {
    private Long id;
    private String certificateNumber;
    private String verificationCode;
    private String studentName;
    private String courseTitle;
    private String certificateUrl;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;
}
