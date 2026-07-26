package com.lms.paymentservice.dto;

import lombok.Data;

@Data
public class CourseCreateRequest {
    /** Optional; auto-generated when omitted. */
    private String courseId;
    private String instructorName;
    private Double amount;
    /** Shown on PayU checkout as product description. Defaults to "Course: {courseId}". */
    private String title;
}
