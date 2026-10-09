package com.lms.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lms.courseservice.enums.LectureType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lectures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String videoUrl;

    private Integer duration;

    private Integer orderIndex;

    private Boolean previewEnabled;

    private String resources;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LectureType type = LectureType.VIDEO;

    @ManyToOne
    @JoinColumn(name = "section_id")
    @JsonIgnore
    private Section section;
}
