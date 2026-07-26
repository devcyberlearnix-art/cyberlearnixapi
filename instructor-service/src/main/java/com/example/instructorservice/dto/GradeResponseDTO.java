package com.example.instructorservice.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeResponseDTO {
    private UUID studentId;
    private Long courseId;
    private Double grade;
    private LocalDateTime updatedAt;
}
