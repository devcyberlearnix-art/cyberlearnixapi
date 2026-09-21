package com.lms.courseservice.repository;

import com.lms.courseservice.entity.CourseMaterials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseMaterialsRepository extends JpaRepository<CourseMaterials, Long> {
    List<CourseMaterials> findByCourseId(Long courseId);
    void deleteByCourseId(Long courseId);
}
