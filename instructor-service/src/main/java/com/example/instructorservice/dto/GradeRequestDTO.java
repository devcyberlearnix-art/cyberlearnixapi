package com.example.instructorservice.dto;


import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeRequestDTO {
    private String studentId;  // student to assign grade (accepts string or UUID formatted string)
    private Object grade;      // grade value (accepts Double, Integer, String numbers, or letter grades like "A", "B", etc.)
}
