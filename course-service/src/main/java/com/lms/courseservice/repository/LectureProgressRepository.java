package com.lms.courseservice.repository;

import com.lms.courseservice.entity.LectureProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LectureProgressRepository extends JpaRepository<LectureProgress, Long> {

    Optional<LectureProgress> findByStudentIdAndLectureId(UUID studentId, Long lectureId);

    List<LectureProgress> findByStudentIdAndCourseId(UUID studentId, Long courseId);

    long countByStudentIdAndCourseIdAndStatus(UUID studentId, Long courseId, LectureProgress.LectureStatus status);

    List<LectureProgress> findByStudentIdAndCourseIdOrderByLastAccessedAtDesc(UUID studentId, Long courseId);

    boolean existsByStudentIdAndLectureId(UUID studentId, Long lectureId);
}
