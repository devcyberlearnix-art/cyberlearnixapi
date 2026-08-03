package com.example.instructorservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String type; // VIDEO / PDF / QUIZ

    @Enumerated(EnumType.STRING)
    private Course.CourseStatus status = Course.CourseStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")   // ✅ ADD THIS
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id") // ✅ ADD THIS
    private Instructor instructor;
    @Column(name = "duration")
    private Integer duration; // store duration in minutes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}