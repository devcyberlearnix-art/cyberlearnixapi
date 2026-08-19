package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {
	List<Course> findByStatusIgnoreCase(String status);

	@Modifying
	@Query("UPDATE Course c SET c.viewCount = COALESCE(c.viewCount, 0) + 1 WHERE c.id = :courseId")
	int incrementViewCount(Long courseId);

	@Modifying
	@Query("UPDATE Course c SET c.searchCount = COALESCE(c.searchCount, 0) + 1, c.viewCount = COALESCE(c.viewCount, 0) + 1 WHERE c.id = :courseId")
	int incrementSearchAndViewCount(Long courseId);

	@Query("SELECT c FROM Course c WHERE c.status = :status " +
	       "AND (:searchPattern IS NULL OR LOWER(c.title) LIKE :searchPattern OR LOWER(c.subtitle) LIKE :searchPattern OR LOWER(c.description) LIKE :searchPattern) " +
	       "AND (:categories IS NULL OR LOWER(c.category) IN :categories) " +
	       "AND (:levels IS NULL OR LOWER(c.level) IN :levels) " +
	       "AND (:languages IS NULL OR LOWER(c.language) IN :languages) " +
	       "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
	       "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " +
	       "AND (:premium IS NULL OR c.premium = :premium) " +
	       "AND (:free IS NULL OR (c.price = 0 OR c.price IS NULL)) " +
	       "AND (:paid IS NULL OR (c.price IS NOT NULL AND c.price > 0)) " +
	       "ORDER BY (COALESCE(c.searchCount, 0) * 0.6 + COALESCE(c.viewCount, 0) * 0.4) DESC")
	Page<Course> findPopularCoursesWithFilters(
	    @Param("status") String status,
	    @Param("searchPattern") String searchPattern,
	    @Param("categories") List<String> categories,
	    @Param("levels") List<String> levels,
	    @Param("languages") List<String> languages,
	    @Param("minPrice") BigDecimal minPrice,
	    @Param("maxPrice") BigDecimal maxPrice,
	    @Param("premium") Boolean premium,
	    @Param("free") Boolean free,
	    @Param("paid") Boolean paid,
	    Pageable pageable
	);

	@Query("SELECT c FROM Course c WHERE c.status = :status " +
	       "AND (:searchPattern IS NULL OR LOWER(c.title) LIKE :searchPattern OR LOWER(c.subtitle) LIKE :searchPattern OR LOWER(c.description) LIKE :searchPattern) " +
	       "AND (:categories IS NULL OR LOWER(c.category) IN :categories) " +
	       "AND (:levels IS NULL OR LOWER(c.level) IN :levels) " +
	       "AND (:languages IS NULL OR LOWER(c.language) IN :languages) " +
	       "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
	       "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " +
	       "AND (:premium IS NULL OR c.premium = :premium) " +
	       "AND (:free IS NULL OR (c.price = 0 OR c.price IS NULL)) " +
	       "AND (:paid IS NULL OR (c.price IS NOT NULL AND c.price > 0)) " +
	       "ORDER BY (SELECT COUNT(e) FROM Enrollment e WHERE e.courseId = c.id) DESC")
	Page<Course> findMostEnrolledCoursesWithFilters(
	    @Param("status") String status,
	    @Param("searchPattern") String searchPattern,
	    @Param("categories") List<String> categories,
	    @Param("levels") List<String> levels,
	    @Param("languages") List<String> languages,
	    @Param("minPrice") BigDecimal minPrice,
	    @Param("maxPrice") BigDecimal maxPrice,
	    @Param("premium") Boolean premium,
	    @Param("free") Boolean free,
	    @Param("paid") Boolean paid,
	    Pageable pageable
	);

}