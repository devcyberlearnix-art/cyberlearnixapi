package com.lms.courseservice.repository;

import com.lms.courseservice.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByAttemptId(Long attemptId);

    List<QuizAnswer> findByAttemptIdAndQuestionId(Long attemptId, Long questionId);

    boolean existsByAttemptIdAndQuestionId(Long attemptId, Long questionId);
}
