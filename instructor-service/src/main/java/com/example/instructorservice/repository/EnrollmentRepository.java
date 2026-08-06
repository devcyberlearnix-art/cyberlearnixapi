package com.example.instructorservice.repository;

import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    // Total enrollments
    long countByCourse(Course course);

    // Active learners
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course = :course AND e.active = true")
    long countActiveByCourse(@Param("course") Course course);

    // ✅ Completion rate query
    @Query("""
        SELECT CASE WHEN COUNT(e2) = 0 THEN 0.0
                    ELSE (COUNT(e) * 100.0 / COUNT(e2))
               END
        FROM Enrollment e
        JOIN Enrollment e2 ON e2.course = :course
        WHERE e.course = :course AND e.status = 'COMPLETED'
    """)
    Double calculateCompletionRate(@Param("course") Course course);

    // Single student enrollment
    Optional<Enrollment> findByCourseAndStudentId(Course course, UUID studentId);

    // All enrollments for a course
    List<Enrollment> findByCourse(Course course);
}