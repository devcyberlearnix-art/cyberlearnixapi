package com.lms.courseservice.service;

import com.lms.courseservice.dto.*;
import com.lms.courseservice.entity.Banner;
import com.lms.courseservice.entity.Banner.BannerStatus;
import com.lms.courseservice.entity.Banner.BannerTargetType;
import com.lms.courseservice.exception.BannerException;
import com.lms.courseservice.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {

    private final BannerRepository bannerRepository;
    private final BannerImageService bannerImageService;

    private static final String ACTIVE_BANNERS_CACHE = "activeBanners";
    private static final String BANNER_CACHE = "banners";

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner createBanner(BannerCreateRequest request, String createdBy) {
        validateDateRange(request.getStartDate(), request.getEndDate());

        if (bannerRepository.existsByDisplayOrder(request.getDisplayOrder())) {
            throw new BannerException("Display order " + request.getDisplayOrder() + " already exists");
        }

        String mobileImageUrl = request.getMobileImageUrl();
        if (mobileImageUrl == null && request.getImageUrl() != null) {
            mobileImageUrl = bannerImageService.generateMobileImageUrl(request.getImageUrl());
        }

        Banner banner = Banner.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .mobileImageUrl(mobileImageUrl)
                .altText(request.getAltText())
                .ctaText(request.getCtaText())
                .ctaUrl(request.getCtaUrl())
                .courseId(request.getCourseId())
                .displayOrder(request.getDisplayOrder())
                .status(request.getStatus())
                .targetType(request.getTargetType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .build();

        Banner savedBanner = bannerRepository.save(banner);
        log.info("Created banner with ID: {} by user: {}", savedBanner.getId(), createdBy);
        return savedBanner;
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner createBanner(BannerCreateRequest request) {
        return createBanner(request, "system");
    }

    @Transactional(readOnly = true)
    public List<Banner> getAllBanners() {
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional(readOnly = true)
    // @Cacheable(value = BANNER_CACHE, key = "#id", unless = "#result == null")
    public Banner getBannerById(Long id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new BannerException("Banner not found with ID: " + id));
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner updateBanner(Long id, BannerUpdateRequest request, String updatedBy) {
        Banner existingBanner = getBannerById(id);
        validateDateRange(request.getStartDate(), request.getEndDate());

        if (!existingBanner.getDisplayOrder().equals(request.getDisplayOrder()) &&
            bannerRepository.existsByDisplayOrderAndIdNot(request.getDisplayOrder(), id)) {
            throw new BannerException("Display order " + request.getDisplayOrder() + " already exists");
        }

        String mobileImageUrl = request.getMobileImageUrl();
        if (mobileImageUrl == null && request.getImageUrl() != null) {
            mobileImageUrl = bannerImageService.generateMobileImageUrl(request.getImageUrl());
        }

        existingBanner.setTitle(request.getTitle());
        existingBanner.setDescription(request.getDescription());
        existingBanner.setImageUrl(request.getImageUrl());
        existingBanner.setMobileImageUrl(mobileImageUrl);
        existingBanner.setAltText(request.getAltText());
        existingBanner.setCtaText(request.getCtaText());
        existingBanner.setCtaUrl(request.getCtaUrl());
        existingBanner.setCourseId(request.getCourseId());
        existingBanner.setDisplayOrder(request.getDisplayOrder());
        existingBanner.setStatus(request.getStatus());
        existingBanner.setTargetType(request.getTargetType());
        existingBanner.setStartDate(request.getStartDate());
        existingBanner.setEndDate(request.getEndDate());
        existingBanner.setUpdatedBy(updatedBy);

        Banner updatedBanner = bannerRepository.save(existingBanner);
        log.info("Updated banner with ID: {} by user: {}", id, updatedBy);
        return updatedBanner;
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner updateBanner(Long id, BannerUpdateRequest request) {
        return updateBanner(id, request, "system");
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner partialUpdateBanner(Long id, BannerPartialUpdateRequest request, String updatedBy) {
        Banner existingBanner = getBannerById(id);

        if (request.getTitle() != null) {
            existingBanner.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            existingBanner.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            existingBanner.setImageUrl(request.getImageUrl());
            String mobileImageUrl = request.getMobileImageUrl();
            if (mobileImageUrl == null) {
                mobileImageUrl = bannerImageService.generateMobileImageUrl(request.getImageUrl());
            }
            existingBanner.setMobileImageUrl(mobileImageUrl);
        }
        if (request.getMobileImageUrl() != null) {
            existingBanner.setMobileImageUrl(request.getMobileImageUrl());
        }
        if (request.getAltText() != null) {
            existingBanner.setAltText(request.getAltText());
        }
        if (request.getCtaText() != null) {
            existingBanner.setCtaText(request.getCtaText());
        }
        if (request.getCtaUrl() != null) {
            existingBanner.setCtaUrl(request.getCtaUrl());
        }
        if (request.getCourseId() != null) {
            existingBanner.setCourseId(request.getCourseId());
        }
        if (request.getDisplayOrder() != null) {
            if (!existingBanner.getDisplayOrder().equals(request.getDisplayOrder()) &&
                bannerRepository.existsByDisplayOrderAndIdNot(request.getDisplayOrder(), id)) {
                throw new BannerException("Display order " + request.getDisplayOrder() + " already exists");
            }
            existingBanner.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getStatus() != null) {
            existingBanner.setStatus(request.getStatus());
        }
        if (request.getTargetType() != null) {
            existingBanner.setTargetType(request.getTargetType());
        }
        if (request.getStartDate() != null || request.getEndDate() != null) {
            validateDateRange(
                request.getStartDate() != null ? request.getStartDate() : existingBanner.getStartDate(),
                request.getEndDate() != null ? request.getEndDate() : existingBanner.getEndDate()
            );
            if (request.getStartDate() != null) {
                existingBanner.setStartDate(request.getStartDate());
            }
            if (request.getEndDate() != null) {
                existingBanner.setEndDate(request.getEndDate());
            }
        }

        existingBanner.setUpdatedBy(updatedBy);
        Banner updatedBanner = bannerRepository.save(existingBanner);
        log.info("Partially updated banner with ID: {} by user: {}", id, updatedBy);
        return updatedBanner;
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner partialUpdateBanner(Long id, BannerPartialUpdateRequest request) {
        return partialUpdateBanner(id, request, "system");
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner updateBannerStatus(Long id, BannerStatusUpdateRequest request, String updatedBy) {
        Banner banner = getBannerById(id);
        banner.setStatus(request.getStatus());
        banner.setUpdatedBy(updatedBy);
        Banner updatedBanner = bannerRepository.save(banner);
        log.info("Updated status for banner with ID: {} to {} by user: {}", id, request.getStatus(), updatedBy);
        return updatedBanner;
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner updateBannerStatus(Long id, BannerStatusUpdateRequest request) {
        return updateBannerStatus(id, request, "system");
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public List<Banner> reorderBanners(BannerReorderRequest request, String updatedBy) {
        for (BannerReorderRequest.BannerOrderItem item : request.getBanners()) {
            Banner banner = getBannerById(item.getId());

            boolean orderExists = bannerRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .anyMatch(b -> b.getDisplayOrder().equals(item.getDisplayOrder()) && !b.getId().equals(item.getId()));

            if (orderExists) {
                throw new BannerException("Display order " + item.getDisplayOrder() + " already exists for another banner");
            }

            banner.setDisplayOrder(item.getDisplayOrder());
            banner.setUpdatedBy(updatedBy);
            bannerRepository.save(banner);
        }

        log.info("Reordered {} banners by user: {}", request.getBanners().size(), updatedBy);
        return bannerRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public List<Banner> reorderBanners(BannerReorderRequest request) {
        return reorderBanners(request, "system");
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public void deleteBanner(Long id, String deletedBy) {
        Banner banner = getBannerById(id);

        bannerImageService.deleteImage(banner.getImageUrl());
        if (banner.getMobileImageUrl() != null && !banner.getMobileImageUrl().equals(banner.getImageUrl())) {
            bannerImageService.deleteImage(banner.getMobileImageUrl());
        }

        banner.setDeletedBy(deletedBy);
        bannerRepository.delete(banner);

        log.info("Soft deleted banner with ID: {} by user: {}", id, deletedBy);
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public void deleteBanner(Long id) {
        deleteBanner(id, "system");
    }

    @Transactional
    @CacheEvict(value = {ACTIVE_BANNERS_CACHE, BANNER_CACHE}, allEntries = true)
    public Banner restoreBanner(Long id, String restoredBy) {
        Banner banner = bannerRepository.findDeletedBannerById(id)
                .orElseThrow(() -> new BannerException("Deleted banner not found with ID: " + id));

        banner.setDeletedAt(null);
        banner.setDeletedBy(null);
        banner.setUpdatedBy(restoredBy);
        Banner restoredBanner = bannerRepository.save(banner);

        log.info("Restored banner with ID: {} by user: {}", id, restoredBy);
        return restoredBanner;
    }

    @Transactional(readOnly = true)
    public List<Banner> getDeletedBanners() {
        return bannerRepository.findDeletedBanners();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ACTIVE_BANNERS_CACHE, key = "'all'")
    public List<PublicBannerResponse> getActiveBanners() {
        Instant currentTime = Instant.now();
        List<Banner> activeBanners = bannerRepository.findActiveBanners(currentTime);

        return activeBanners.stream()
                .map(this::toPublicBannerResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = ACTIVE_BANNERS_CACHE, key = "'targetType:' + #targetType")
    public List<PublicBannerResponse> getActiveBannersByTargetType(BannerTargetType targetType) {
        Instant currentTime = Instant.now();
        List<Banner> activeBanners = bannerRepository.findActiveBannersByTargetType(targetType, currentTime);

        return activeBanners.stream()
                .map(this::toPublicBannerResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void trackImpression(Long bannerId) {
        if (bannerRepository.existsById(bannerId)) {
            bannerRepository.incrementImpressions(bannerId, Instant.now());
            log.debug("Recorded impression for banner ID: {}", bannerId);
        }
    }

    @Transactional
    public void trackClick(Long bannerId) {
        if (bannerRepository.existsById(bannerId)) {
            bannerRepository.incrementClicks(bannerId, Instant.now());
            log.debug("Recorded click for banner ID: {}", bannerId);
        }
    }

    @Transactional
    public void trackConversion(Long bannerId) {
        if (bannerRepository.existsById(bannerId)) {
            bannerRepository.incrementConversions(bannerId);
            log.debug("Recorded conversion for banner ID: {}", bannerId);
        }
    }

    @Transactional(readOnly = true)
    public BannerAnalyticsResponse getBannerAnalytics(Long bannerId) {
        Banner banner = getBannerById(bannerId);

        BigDecimal ctr = BigDecimal.ZERO;
        if (banner.getImpressions() != null && banner.getImpressions() > 0) {
            long clicks = banner.getClicks() != null ? banner.getClicks() : 0;
            ctr = BigDecimal.valueOf(clicks)
                    .divide(BigDecimal.valueOf(banner.getImpressions()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal conversionRate = BigDecimal.ZERO;
        if (banner.getClicks() != null && banner.getClicks() > 0) {
            long conversions = banner.getConversions() != null ? banner.getConversions() : 0;
            conversionRate = BigDecimal.valueOf(conversions)
                    .divide(BigDecimal.valueOf(banner.getClicks()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BannerAnalyticsResponse.builder()
                .bannerId(banner.getId())
                .impressions(banner.getImpressions() != null ? banner.getImpressions() : 0L)
                .clicks(banner.getClicks() != null ? banner.getClicks() : 0L)
                .conversions(banner.getConversions() != null ? banner.getConversions() : 0L)
                .ctr(ctr)
                .conversionRate(conversionRate)
                .lastImpressionAt(banner.getLastImpressionAt())
                .lastClickAt(banner.getLastClickAt())
                .build();
    }

    private void validateDateRange(Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BannerException("Start date must be before end date");
        }
    }

    private PublicBannerResponse toPublicBannerResponse(Banner banner) {
        return PublicBannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .description(banner.getDescription())
                .imageUrl(banner.getImageUrl())
                .mobileImageUrl(banner.getMobileImageUrl())
                .altText(banner.getAltText())
                .ctaText(banner.getCtaText())
                .ctaUrl(banner.getCtaUrl())
                .courseId(banner.getCourseId())
                .displayOrder(banner.getDisplayOrder())
                .targetType(banner.getTargetType())
                .build();
    }
}
