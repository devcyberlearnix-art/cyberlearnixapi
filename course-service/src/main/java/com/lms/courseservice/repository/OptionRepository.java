package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Long> {

    List<Option> findByQuestionId(Long questionId);

    List<Option> findByQuestionIdOrderByOrderIndexAsc(Long questionId);

    boolean existsByQuestionId(Long questionId);
}
