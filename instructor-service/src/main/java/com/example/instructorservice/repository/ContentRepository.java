package com.example.instructorservice.repository;

import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {

    @Query("SELECT COUNT(s) FROM Content s WHERE s.course = :course AND s.type = 'SECTION'")
    int countSectionsByCourse(Course course);

    @Query("SELECT COUNT(l) FROM Content l WHERE l.course = :course AND l.type = 'LECTURE'")
    int countLecturesByCourse(Course course);

    @Query("SELECT COUNT(a) FROM Content a WHERE a.course = :course AND a.type = 'ASSIGNMENT'")
    int countAssignmentsByCourse(Course course);

    @Query("SELECT COUNT(q) FROM Content q WHERE q.course = :course AND q.type = 'QUIZ'")
    int countQuizzesByCourse(Course course);
    @Query("SELECT COALESCE(SUM(c.duration), 0) FROM Content c WHERE c.course = :course")
    int totalDurationByCourse(Course course);

    @Query("SELECT c FROM Content c WHERE c.id = :contentId AND c.instructor.id = :instructorId")
    Optional<Content> findByIdAndInstructorId(@Param("contentId") UUID contentId, @Param("instructorId") UUID instructorId);

}