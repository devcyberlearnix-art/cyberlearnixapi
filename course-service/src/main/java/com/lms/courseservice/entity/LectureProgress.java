package com.lms.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lecture_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "lecture_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LectureProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LectureStatus status = LectureStatus.NOT_STARTED;

    @Column(nullable = false)
    @Builder.Default
    private Integer progressPercentage = 0;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastAccessedAt;

    private Integer timeSpentSeconds;

    public enum LectureStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }
}
