package com.cyberlearnix.commonlibs.repository.analytics;

import com.cyberlearnix.commonlibs.dto.analytics.ZipcodeFrequentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for querying mv_zipcode_frequent_products materialized view
 */
@Repository
public interface ZipcodeFrequentProductRepository extends JpaRepository<ZipcodeFrequentProduct, Long> {
    
    /**
     * Get frequently bought products for a zipcode
     */
        @Query(value = "SELECT m.product_id, p.gtin, m.product_name, p.brand_name, p.front_image_url " +
            "FROM mv_zipcode_frequent_products m " +
            "LEFT JOIN products p ON m.product_id = p.product_id " +
            "WHERE m.zipcode = :zipcode AND m.rank_in_zipcode <= :limit " +
            "ORDER BY m.rank_in_zipcode", nativeQuery = true)
    List<Object[]> findByZipcodeAndRankLimit(@Param("zipcode") String zipcode, @Param("limit") int limit);
}
