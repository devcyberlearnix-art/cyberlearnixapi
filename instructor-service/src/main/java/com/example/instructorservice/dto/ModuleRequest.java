package com.example.instructorservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleRequest {

    @NotBlank(message = "Module title is required")
    @Size(max = 255, message = "Module title must be at most 255 characters")
    private String title;

    @NotBlank(message = "Module description is required")
    @Size(max = 2000, message = "Module description must be at most 2000 characters")
    private String description;
}
