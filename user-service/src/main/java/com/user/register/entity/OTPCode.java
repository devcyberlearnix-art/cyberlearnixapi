package com.user.register.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTPCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    @Builder.Default
    private int remainingAttempts = 5; // default 5 attempts
    private String otp;
    private String type; // registration, password_reset, login
    private LocalDateTime expiresAt;
    @Builder.Default
    private Boolean verified = false;
    @Builder.Default
    @Column(nullable = false)
    private Integer attempts = 0; // can be null initially if you want
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}