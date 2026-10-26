package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourseId(Long courseId);

    List<Assignment> findBySectionId(Long sectionId);

    Optional<Assignment> findByIdAndCourseId(Long id, Long courseId);

    List<Assignment> findByCourseIdAndStatus(Long courseId, Assignment.AssignmentStatus status);

    boolean existsBySectionIdAndCourseId(Long sectionId, Long courseId);

    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.courseId IN :courseIds AND a.status = 'PUBLISHED'")
    long countPublishedByCourseIds(@Param("courseIds") List<Long> courseIds);
}