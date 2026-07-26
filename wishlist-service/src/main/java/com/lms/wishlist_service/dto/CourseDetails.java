package com.lms.wishlist_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseDetails {

    @JsonProperty("id") // Maps JSON 'id' to Java 'courseId'
    private String courseId;

    private String title;
    private String subtitle;
    private String instructorId;
    private String courseName;
    private BigDecimal price;

    @Builder.Default
    private String currency = "INR"; // Default for your professional LMS

    private String thumbnail;
    private String category;
    private String level;
}