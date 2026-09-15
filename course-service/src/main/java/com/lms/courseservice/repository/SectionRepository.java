package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findByCourseId(Long courseId);

    @Query("SELECT s FROM Section s LEFT JOIN FETCH s.lectures WHERE s.course.id = :courseId ORDER BY s.orderIndex")
    List<Section> findByCourseIdWithLectures(@org.springframework.data.repository.query.Param("courseId") Long courseId);

}