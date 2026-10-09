package com.example.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAnnouncementRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    // Optional: target specific users
    private List<UUID> userIds;

    // Optional scheduling
    private LocalDateTime scheduledAt;

    @NotNull
    private Boolean sendToAll; // true = broadcast

    private Long courseId;
    private UUID createdBy;
}
