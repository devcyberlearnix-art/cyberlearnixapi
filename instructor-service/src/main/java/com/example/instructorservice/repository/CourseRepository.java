package com.example.instructorservice.repository;


import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructor(Instructor instructor);
    Optional<Course> findByIdAndInstructor(Long courseId, Instructor instructor);

    @Query("SELECT c FROM Course c WHERE c.id = :courseId AND c.instructor.id = :instructorId")
    Optional<Course> findByIdAndInstructorId(@Param("courseId") Long courseId, @Param("instructorId") UUID instructorId);

    List<Course> findByInstructorId(UUID instructorId);
}

