package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizId(Long quizId);

    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);

    boolean existsByQuizId(Long quizId);
}
