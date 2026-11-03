package com.lms.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollment", uniqueConstraints = {
    @UniqueConstraint(name = "uk_enrollment_student_course", columnNames = {"studentId", "courseId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Student is UUID
    private UUID studentId;

    // Course id stored as numeric Long (matches courses.id)
    private Long courseId;

    private String studentName;

    private LocalDateTime enrolledAt;

    private String status;

    private Double progress;

    private LocalDateTime completedAt;

    private Long lastLectureId;
}
