package com.lms.courseservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveClassRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    private Long sectionId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Meeting URL is required")
    @Size(max = 1000, message = "Meeting URL must not exceed 1000 characters")
    @Pattern(regexp = "^(https?|ftp)://.*", message = "Meeting URL must be a valid URL")
    private String meetingUrl;

    @NotNull(message = "Meeting provider is required")
    private String meetingProvider;

    @NotNull(message = "Scheduled start time is required")
    private LocalDateTime scheduledStartTime;

    @NotNull(message = "Scheduled end time is required")
    private LocalDateTime scheduledEndTime;

    @Positive(message = "Max participants must be positive")
    private Integer maxParticipants;
}
