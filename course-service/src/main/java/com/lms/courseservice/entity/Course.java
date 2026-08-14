package com.lms.courseservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;
// Course id uses numeric DB primary key

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String subtitle;

    @Column(length = 2000)
    private String description;

    private String category;

    private String level;

    private String language;

    private BigDecimal price;

    private String thumbnail;

    private UUID instructorId;

    private String status; // Draft, Published, Archived

    @Builder.Default
    private Boolean premium = false;

    @Builder.Default
    private Long searchCount = 0L;

    @Builder.Default
    private Long viewCount = 0L;
}