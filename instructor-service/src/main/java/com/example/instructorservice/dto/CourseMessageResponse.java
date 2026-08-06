package com.example.instructorservice.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CourseMessageResponse {

    private UUID messageId;
    private Long courseId;
    private UUID instructorId;

    private String subject;
    private String message;

    private LocalDateTime sentAt;

    private String status;
    private String deliveryInfo;
}
