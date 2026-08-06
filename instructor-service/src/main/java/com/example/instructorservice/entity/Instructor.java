package com.example.instructorservice.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_NULL)

@Entity
@Getter
@Setter
@Table(name = "instructor")
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // UUID primary key
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;   // maps to User entity if you have one

    @Column(nullable = false, length = 255)
    private String name;
    @Column(length = 255, unique = true)
    private String email; // remove nullable = false
    // Optional: One instructor can have many courses
    private String headline;    // e.g., "Java Developer"
    private Double rating;      // e.g., 4.5
    private Boolean verified;   // true/false
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses;
}