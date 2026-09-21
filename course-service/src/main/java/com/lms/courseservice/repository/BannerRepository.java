package com.lms.courseservice.repository;

import com.lms.courseservice.entity.Banner;
import com.lms.courseservice.entity.Banner.BannerStatus;
import com.lms.courseservice.entity.Banner.BannerTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findAllByOrderByDisplayOrderAsc();

    @Query("SELECT b FROM Banner b WHERE b.status = 'ACTIVE' " +
           "AND (b.startDate IS NULL OR b.startDate <= :currentTime) " +
           "AND (b.endDate IS NULL OR b.endDate >= :currentTime) " +
           "ORDER BY b.displayOrder ASC")
    List<Banner> findActiveBanners(@Param("currentTime") Instant currentTime);

    @Query("SELECT b FROM Banner b WHERE b.id = :id " +
           "AND b.status = 'ACTIVE' " +
           "AND (b.startDate IS NULL OR b.startDate <= :currentTime) " +
           "AND (b.endDate IS NULL OR b.endDate >= :currentTime)")
    Optional<Banner> findActiveBannerById(@Param("id") Long id, @Param("currentTime") Instant currentTime);

    @Query("SELECT b FROM Banner b WHERE b.displayOrder = :displayOrder AND b.status = 'ACTIVE'")
    Optional<Banner> findActiveBannerByDisplayOrder(@Param("displayOrder") Integer displayOrder);

    @Query("SELECT b FROM Banner b WHERE b.targetType = :targetType AND b.status = 'ACTIVE' " +
           "AND (b.startDate IS NULL OR b.startDate <= :currentTime) " +
           "AND (b.endDate IS NULL OR b.endDate >= :currentTime) " +
           "ORDER BY b.displayOrder ASC")
    List<Banner> findActiveBannersByTargetType(@Param("targetType") BannerTargetType targetType,
                                                  @Param("currentTime") Instant currentTime);

    @Modifying
    @Query("UPDATE Banner b SET b.impressions = COALESCE(b.impressions, 0) + 1, b.lastImpressionAt = :timestamp WHERE b.id = :bannerId")
    int incrementImpressions(@Param("bannerId") Long bannerId, @Param("timestamp") Instant timestamp);

    @Modifying
    @Query("UPDATE Banner b SET b.clicks = COALESCE(b.clicks, 0) + 1, b.lastClickAt = :timestamp WHERE b.id = :bannerId")
    int incrementClicks(@Param("bannerId") Long bannerId, @Param("timestamp") Instant timestamp);

    @Modifying
    @Query("UPDATE Banner b SET b.conversions = COALESCE(b.conversions, 0) + 1 WHERE b.id = :bannerId")
    int incrementConversions(@Param("bannerId") Long bannerId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Banner b " +
           "WHERE b.displayOrder = :displayOrder")
    boolean existsByDisplayOrder(@Param("displayOrder") Integer displayOrder);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Banner b " +
           "WHERE b.displayOrder = :displayOrder AND b.id != :excludeId")
    boolean existsByDisplayOrderAndIdNot(@Param("displayOrder") Integer displayOrder, @Param("excludeId") Long excludeId);

    @Query("SELECT b FROM Banner b WHERE b.deletedAt IS NOT NULL ORDER BY b.deletedAt DESC")
    List<Banner> findDeletedBanners();

    @Query("SELECT b FROM Banner b WHERE b.id = :id AND b.deletedAt IS NOT NULL")
    Optional<Banner> findDeletedBannerById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Banner b SET b.deletedAt = NULL, b.deletedBy = NULL WHERE b.id = :bannerId")
    int restoreBanner(@Param("bannerId") Long bannerId);
}
