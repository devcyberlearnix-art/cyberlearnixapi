package com.lms.review.repository;

import com.lms.review.entity.Review;
import com.lms.review.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUuid(UUID uuid);

    Optional<Review> findByUserIdAndCourseId(UUID userId, Long courseId);

    Page<Review> findByCourseIdAndStatus(Long courseId, ReviewStatus status, Pageable pageable);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.courseId = :courseId AND r.status = :status GROUP BY r.rating")
    List<Object[]> countRatingsByCourseIdAndStatus(@Param("courseId") Long courseId,
            @Param("status") ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.courseId = :courseId AND r.status = :status")
    Double averageRatingByCourseIdAndStatus(@Param("courseId") Long courseId,
            @Param("status") ReviewStatus status);

    long countByCourseIdAndStatus(Long courseId, ReviewStatus status);
}
