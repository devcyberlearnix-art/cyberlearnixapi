package com.lms.courseservice.repository;

import com.lms.courseservice.entity.CourseFAQ;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseFAQRepository extends JpaRepository<CourseFAQ, Long> {
    List<CourseFAQ> findByCourseIdOrderByDisplayOrderAsc(Long courseId);
    void deleteByCourseId(Long courseId);
}
