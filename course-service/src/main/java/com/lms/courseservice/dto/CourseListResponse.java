package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseListResponse {
    private Boolean success;
    private String message;
    private CourseListData data;
    private String timestamp;
}
