package com.example.instructorservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "completion_rate")
    private Double completionRate; // 0.0 to 100.0

    @Builder.Default
    private Boolean active = true;  // mark active by default

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;  // store the user’s ID directly

    @Column(name = "status")
    @Builder.Default
    private String status = "ENROLLED"; // default status

    @Column(name = "enrolled_at", nullable = false)
    @Builder.Default
    private LocalDateTime enrolledAt = LocalDateTime.now();

    @Column(name = "last_activity_at")
    @Builder.Default
    private LocalDateTime lastActivityAt = LocalDateTime.now();
    @Column(name = "grade")
    private Double grade;
    // ❌ Remove any getStudent() method — it doesn't exist
}