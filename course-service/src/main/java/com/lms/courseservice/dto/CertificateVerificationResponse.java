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
public class CertificateVerificationResponse {
    private boolean valid;
    private String certificateNumber;
    private String studentName;
    private String courseTitle;
    private String status;
    private LocalDateTime issuedAt;
    private String message;
}
