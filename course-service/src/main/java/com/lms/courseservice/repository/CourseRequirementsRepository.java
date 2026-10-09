package com.lms.courseservice.repository;

import com.lms.courseservice.entity.CourseRequirements;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRequirementsRepository extends JpaRepository<CourseRequirements, Long> {
    List<CourseRequirements> findByCourseId(Long courseId);
    void deleteByCourseId(Long courseId);
}
