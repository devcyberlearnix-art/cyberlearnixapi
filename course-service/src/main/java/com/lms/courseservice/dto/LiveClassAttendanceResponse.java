package com.lms.courseservice.dto;

import com.lms.courseservice.entity.LiveClassAttendance;
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
public class LiveClassAttendanceResponse {

    private Long id;
    private Long liveClassId;
    private UUID studentId;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Long durationSeconds;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LiveClassAttendanceResponse fromEntity(LiveClassAttendance attendance) {
        return LiveClassAttendanceResponse.builder()
                .id(attendance.getId())
                .liveClassId(attendance.getLiveClassId())
                .studentId(attendance.getStudentId())
                .joinedAt(attendance.getJoinedAt())
                .leftAt(attendance.getLeftAt())
                .durationSeconds(attendance.getDurationSeconds())
                .status(attendance.getStatus() != null ? attendance.getStatus().name() : null)
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
}
