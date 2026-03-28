package com.cyberlearnix.commonlibs.repository.analytics;

import com.cyberlearnix.commonlibs.dto.analytics.ZipcodeTrendingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying mv_zipcode_trending_categories materialized view
 */
@Repository
public interface ZipcodeTrendingCategoryRepository extends JpaRepository<ZipcodeTrendingCategory, String> {
    
    /**
     * Get trending categories for a zipcode
     */
        @Query(value = "SELECT c.id, m.category, c.display_name, c.image_url, c.slug " +
            "FROM mv_zipcode_trending_categories m " +
            "LEFT JOIN categories c ON m.category = c.name " +
            "WHERE m.zipcode = :zipcode AND m.rank_in_zipcode <= :limit " +
            "ORDER BY m.rank_in_zipcode", nativeQuery = true)
    List<Object[]> findByZipcodeAndRankLimit(@Param("zipcode") String zipcode, @Param("limit") int limit);
}
