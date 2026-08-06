package com.example.instructorservice.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class InstructorDTO {
    private UUID instructorId;
    private String name;
    private String email;       // Optional, if you want to include email
    private String headline;    // e.g., "Java Developer"
    private Double rating;      // e.g., 4.5
    private Boolean verified;   // true/false
}
