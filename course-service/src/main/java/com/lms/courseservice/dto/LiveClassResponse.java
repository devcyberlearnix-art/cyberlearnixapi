package com.lms.courseservice.dto;

import com.lms.courseservice.entity.LiveClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveClassResponse {

    private Long id;
    private Long courseId;
    private Long sectionId;
    private UUID instructorId;
    private String title;
    private String description;
    private String meetingUrl;
    private String meetingProvider;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private String status;
    private String recordingUrl;
    private Integer maxParticipants;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LiveClassResponse fromEntity(LiveClass liveClass) {
        return LiveClassResponse.builder()
                .id(liveClass.getId())
                .courseId(liveClass.getCourseId())
                .sectionId(liveClass.getSectionId())
                .instructorId(liveClass.getInstructorId())
                .title(liveClass.getTitle())
                .description(liveClass.getDescription())
                .meetingUrl(liveClass.getMeetingUrl())
                .meetingProvider(liveClass.getMeetingProvider() != null ? liveClass.getMeetingProvider().name() : null)
                .scheduledStartTime(liveClass.getScheduledStartTime())
                .scheduledEndTime(liveClass.getScheduledEndTime())
                .status(liveClass.getStatus() != null ? liveClass.getStatus().name() : null)
                .recordingUrl(liveClass.getRecordingUrl())
                .maxParticipants(liveClass.getMaxParticipants())
                .cancelledAt(liveClass.getCancelledAt())
                .createdAt(liveClass.getCreatedAt())
                .updatedAt(liveClass.getUpdatedAt())
                .build();
    }
}
