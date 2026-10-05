package com.user.register.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSession {

    @Id
    @GeneratedValue
    @UuidGenerator

    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String deviceInfo;   // optional: raw browser/device info
    private String deviceId;
    private String deviceName;
    private String deviceType;
    private String browser;
    private String operatingSystem;
    private Double latitude;
    private Double longitude;
    private String city;
    private String country;

    @Column(columnDefinition = "TEXT")
    private String token;  // if needed
    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private String ipAddress;
    private LocalDateTime expiresAt;
}