package com.example.instructorservice.dto;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRequestDTO {
    private UUID studentId;  // student to assign grade
    private Double grade;    // grade value
}
