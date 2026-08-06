package com.example.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue
    private UUID id;
    private UUID userId;
    private LocalDateTime updatedAt; // ✅ ADD THIS
    private boolean emailEnabled;
    private boolean pushEnabled;
    private boolean inAppEnabled;
    public void setSmsEnabled(Boolean smsEnabled) {

    }
}