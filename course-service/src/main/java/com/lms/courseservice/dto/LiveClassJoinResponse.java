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
public class LiveClassJoinResponse {

    private Long id;
    private String title;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private String status;
    private String meetingProvider;
    private String meetingUrl;
}
