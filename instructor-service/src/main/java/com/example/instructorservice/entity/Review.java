package com.example.instructorservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    private Course course;


    @Column(name = "student_id", nullable = false)
    private UUID studentId;  // store user ID directly

    private double rating;
    private String comment;
    private LocalDateTime createdAt;
}
