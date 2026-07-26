package com.example.admin.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourseListResponse {

    private boolean success;
    private String message;
    private List<Object> data;
    private int count;
    private String timestamp;
}
