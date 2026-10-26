package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeLearningResponse {
    private Long courseId;
    private String title;
    private Long lastLectureId;
    private String lastLectureTitle;
    private Long nextLectureId;
    private String nextLectureTitle;
    private String status;
}
