package com.lms.cart_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetails {
    @JsonProperty("id")
    private Long courseId;
    private String title;
    private Double price;
    @JsonProperty("instructorId")
    private String instructorId;

    public Long getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return title;
    }

    public String getInstructorId() {
        return instructorId;
    }
}