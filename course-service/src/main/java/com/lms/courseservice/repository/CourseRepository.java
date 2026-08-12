package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
	List<Course> findByStatusIgnoreCase(String status);

	@Modifying
	@Query("UPDATE Course c SET c.viewCount = COALESCE(c.viewCount, 0) + 1 WHERE c.id = :courseId")
	int incrementViewCount(Long courseId);

	@Modifying
	@Query("UPDATE Course c SET c.searchCount = COALESCE(c.searchCount, 0) + 1, c.viewCount = COALESCE(c.viewCount, 0) + 1 WHERE c.id = :courseId")
	int incrementSearchAndViewCount(Long courseId);
}