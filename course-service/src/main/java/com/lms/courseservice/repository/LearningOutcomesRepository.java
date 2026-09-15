package com.lms.courseservice.repository;

import com.lms.courseservice.entity.LearningOutcomes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningOutcomesRepository extends JpaRepository<LearningOutcomes, Long> {
    List<LearningOutcomes> findByCourseId(Long courseId);
    void deleteByCourseId(Long courseId);
}