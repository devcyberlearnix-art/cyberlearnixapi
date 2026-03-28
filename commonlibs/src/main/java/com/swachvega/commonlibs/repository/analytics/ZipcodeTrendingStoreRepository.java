package com.cyberlearnix.commonlibs.repository.analytics;

import com.cyberlearnix.commonlibs.dto.analytics.ZipcodeTrendingStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying mv_zipcode_trending_stores materialized view
 * Note: This is a read-only repository as materialized views cannot be modified directly
 */
@Repository
public interface ZipcodeTrendingStoreRepository extends JpaRepository<ZipcodeTrendingStore, Long> {
    
    /**
     * Get trending stores for a zipcode, ordered by rank
     */
    @Query(value = "SELECT m.store_id, m.store_name, s.image_url FROM mv_zipcode_trending_stores m " +
            "LEFT JOIN stores s ON m.store_id = s.store_id " +
            "WHERE m.zipcode = :zipcode AND m.rank_in_zipcode <= :limit " +
            "ORDER BY m.rank_in_zipcode", nativeQuery = true)
    List<Object[]> findByZipcodeAndRankLimit(@Param("zipcode") String zipcode, @Param("limit") int limit);
    
    /**
     * Get premium stores for a zipcode
     */
    @Query(value = "SELECT s.store_id, s.store_name, s.image_url FROM stores s " +
            "WHERE s.zip_code = :zipcode AND s.premium_status = true AND s.is_active = true " +
            "ORDER BY s.rating DESC, s.store_id ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findPremiumStoresByZipcode(@Param("zipcode") String zipcode, @Param("limit") int limit);
    
    /**
     * Get stores in a locality (fallback when no trending stores in zipcode)
     */
    @Query(value = "SELECT s.store_id, s.store_name, s.image_url FROM stores s " +
            "WHERE s.is_active = true AND (LOWER(s.city) = LOWER(:locality) OR LOWER(s.state) = LOWER(:locality)) " +
            "ORDER BY s.premium_status DESC, s.rating DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> findByLocality(@Param("locality") String locality, @Param("limit") int limit);
}
