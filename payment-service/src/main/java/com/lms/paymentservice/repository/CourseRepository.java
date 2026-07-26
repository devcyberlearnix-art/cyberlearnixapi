package com.lms.paymentservice.repository;

import com.lms.paymentservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseId(String courseId);

    boolean existsByCourseId(String courseId);

    List<Course> findAllByOrderByCreatedAtDesc();
}
