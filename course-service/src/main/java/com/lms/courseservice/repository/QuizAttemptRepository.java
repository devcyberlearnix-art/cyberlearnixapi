package com.lms.courseservice.repository;

import com.lms.courseservice.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Optional<QuizAttempt> findByQuizIdAndStudentIdAndStatus(Long quizId, UUID studentId, QuizAttempt.AttemptStatus status);

    List<QuizAttempt> findByQuizIdAndStudentId(Long quizId, UUID studentId);

    List<QuizAttempt> findByStudentId(UUID studentId);

    Optional<QuizAttempt> findFirstByQuizIdAndStudentIdOrderByAttemptNumberDesc(Long quizId, UUID studentId);

    Long countByQuizIdAndStudentId(Long quizId, UUID studentId);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.studentId = :studentId")
    long countByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT AVG(qa.percentage) FROM QuizAttempt qa WHERE qa.studentId = :studentId AND qa.percentage IS NOT NULL")
    Double averagePercentageByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.studentId = :studentId AND qa.passed = true")
    long countPassedByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.studentId = :studentId AND qa.passed = false")
    long countFailedByStudentId(@Param("studentId") UUID studentId);
}
