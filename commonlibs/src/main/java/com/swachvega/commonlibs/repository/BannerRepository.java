package com.cyberlearnix.commonlibs.repository;

import com.cyberlearnix.commonlibs.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    /**
     * Find all active banners by position, ordered by sort order
     */
    @Query("SELECT b FROM Banner b WHERE b.isActive = true AND b.position = :position " +
           "AND (b.startDate IS NULL OR b.startDate <= :now) " +
           "AND (b.endDate IS NULL OR b.endDate >= :now) " +
           "ORDER BY b.sortOrder ASC")
    List<Banner> findActiveByPosition(@Param("position") Banner.BannerPosition position, 
                                     @Param("now") LocalDateTime now);

    /**
     * Find all active banners by type, ordered by sort order
     */
    @Query("SELECT b FROM Banner b WHERE b.isActive = true AND b.type = :type " +
           "AND (b.startDate IS NULL OR b.startDate <= :now) " +
           "AND (b.endDate IS NULL OR b.endDate >= :now) " +
           "ORDER BY b.sortOrder ASC")
    List<Banner> findActiveByType(@Param("type") Banner.BannerType type, 
                                 @Param("now") LocalDateTime now);

    /**
     * Find all currently active banners ordered by position and sort order
     */
    @Query("SELECT b FROM Banner b WHERE b.isActive = true " +
           "AND (b.startDate IS NULL OR b.startDate <= :now) " +
           "AND (b.endDate IS NULL OR b.endDate >= :now) " +
           "ORDER BY b.position, b.sortOrder ASC")
    List<Banner> findAllActive(@Param("now") LocalDateTime now);

    /**
     * Find banners by position regardless of active status
     */
    List<Banner> findByPositionOrderBySortOrderAsc(Banner.BannerPosition position);

    /**
     * Find banners by type regardless of active status
     */
    List<Banner> findByTypeOrderBySortOrderAsc(Banner.BannerType type);
}
