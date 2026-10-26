package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCourseId(Long courseId);

    List<Quiz> findBySectionId(Long sectionId);

    Optional<Quiz> findByIdAndCourseId(Long id, Long courseId);

    List<Quiz> findByCourseIdAndStatus(Long courseId, Quiz.QuizStatus status);

    boolean existsBySectionIdAndCourseId(Long sectionId, Long courseId);
}
