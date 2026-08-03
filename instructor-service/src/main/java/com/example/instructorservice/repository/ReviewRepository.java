package com.example.instructorservice.repository;


import com.example.instructorservice.entity.Course;
import com.example.instructorservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.course = :course")
    double averageRatingByCourse(Course course);
    long countByCourse(Course course);

}
