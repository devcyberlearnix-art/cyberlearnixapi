package com.lms.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonIgnore
    private Course course;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Lecture> lectures = new ArrayList<>();
}
