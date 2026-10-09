package com.lms.courseservice.service;

import com.lms.courseservice.dto.*;
import com.lms.courseservice.entity.Banner;
import com.lms.courseservice.entity.Banner.BannerStatus;
import com.lms.courseservice.entity.Banner.BannerTargetType;
import com.lms.courseservice.exception.BannerException;
import com.lms.courseservice.repository.BannerRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BannerServiceTest {

    @Mock
    private BannerRepository bannerRepository;

    @Mock
    private BannerImageService bannerImageService;

    @InjectMocks
    private BannerService bannerService;

    @Test
    void createBanner_shouldCreateBannerSuccessfully() {
        BannerCreateRequest request = BannerCreateRequest.builder()
                .title("Test Banner")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .ctaText("Click Here")
                .ctaUrl("/courses/1")
                .displayOrder(1)
                .status(BannerStatus.ACTIVE)
                .targetType(BannerTargetType.COURSE)
                .build();

        when(bannerRepository.existsByDisplayOrder(1)).thenReturn(false);
        when(bannerImageService.generateMobileImageUrl(anyString())).thenReturn("https://example.com/mobile-image.jpg");
        when(bannerRepository.save(any(Banner.class))).thenAnswer(invocation -> {
            Banner banner = invocation.getArgument(0);
            banner.setId(1L);
            return banner;
        });

        Banner result = bannerService.createBanner(request, "test-user");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Banner");
        assertThat(result.getDisplayOrder()).isEqualTo(1);
        assertThat(result.getTargetType()).isEqualTo(BannerTargetType.COURSE);
        assertThat(result.getCreatedBy()).isEqualTo("test-user");
        verify(bannerRepository).save(any(Banner.class));
    }

    @Test
    void createBanner_shouldCreateBannerSuccessfully_withoutTargetType() {
        BannerCreateRequest request = BannerCreateRequest.builder()
                .title("Test Banner")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .ctaText("Click Here")
                .ctaUrl("/courses/1")
                .displayOrder(1)
                .status(BannerStatus.ACTIVE)
                .build();

        when(bannerRepository.existsByDisplayOrder(1)).thenReturn(false);
        when(bannerImageService.generateMobileImageUrl(anyString())).thenReturn("https://example.com/mobile-image.jpg");
        when(bannerRepository.save(any(Banner.class))).thenAnswer(invocation -> {
            Banner banner = invocation.getArgument(0);
            banner.setId(1L);
            return banner;
        });

        Banner result = bannerService.createBanner(request, "test-user");

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Banner");
        assertThat(result.getDisplayOrder()).isEqualTo(1);
        assertThat(result.getTargetType()).isNull();
        assertThat(result.getCreatedBy()).isEqualTo("test-user");
        verify(bannerRepository).save(any(Banner.class));
    }

    @Test
    void createBanner_shouldThrowException_whenDisplayOrderExists() {
        BannerCreateRequest request = BannerCreateRequest.builder()
                .title("Test Banner")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .ctaText("Click Here")
                .ctaUrl("/courses/1")
                .displayOrder(1)
                .status(BannerStatus.ACTIVE)
                .build();

        when(bannerRepository.existsByDisplayOrder(1)).thenReturn(true);

        assertThatThrownBy(() -> bannerService.createBanner(request, "test-user"))
                .isInstanceOf(BannerException.class)
                .hasMessageContaining("Display order 1 already exists");
    }

    @Test
    void createBanner_shouldThrowException_whenStartDateAfterEndDate() {
        BannerCreateRequest request = BannerCreateRequest.builder()
                .title("Test Banner")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .ctaText("Click Here")
                .ctaUrl("/courses/1")
                .displayOrder(1)
                .status(BannerStatus.ACTIVE)
                .startDate(Instant.parse("2026-12-31T00:00:00Z"))
                .endDate(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        assertThatThrownBy(() -> bannerService.createBanner(request, "test-user"))
                .isInstanceOf(BannerException.class)
                .hasMessageContaining("Start date must be before end date");
    }

    @Test
    void getAllBanners_shouldReturnAllBanners() {
        Banner banner1 = Banner.builder().id(1L).displayOrder(1).build();
        Banner banner2 = Banner.builder().id(2L).displayOrder(2).build();

        when(bannerRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(Arrays.asList(banner1, banner2));

        List<Banner> result = bannerService.getAllBanners();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDisplayOrder()).isLessThan(result.get(1).getDisplayOrder());
    }

    @Test
    void getBannerById_shouldReturnBanner_whenExists() {
        Banner banner = Banner.builder()
                .id(1L)
                .title("Test Banner")
                .build();

        when(bannerRepository.findById(1L)).thenReturn(Optional.of(banner));

        Banner result = bannerService.getBannerById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBannerById_shouldThrowException_whenNotExists() {
        when(bannerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bannerService.getBannerById(1L))
                .isInstanceOf(BannerException.class)
                .hasMessageContaining("Banner not found");
    }

    @Test
    void updateBanner_shouldUpdateBannerSuccessfully() {
        Banner existingBanner = Banner.builder()
                .id(1L)
                .title("Old Title")
                .displayOrder(1)
                .build();

        BannerUpdateRequest request = BannerUpdateRequest.builder()
                .title("New Title")
                .description("New Description")
                .imageUrl("https://example.com/new-image.jpg")
                .ctaText("New CTA")
                .ctaUrl("/courses/2")
                .displayOrder(1)
                .status(BannerStatus.ACTIVE)
                .targetType(BannerTargetType.CATEGORY)
                .build();

        lenient().when(bannerRepository.findById(1L)).thenReturn(Optional.of(existingBanner));
        lenient().when(bannerRepository.existsByDisplayOrderAndIdNot(1, 1L)).thenReturn(false);
        when(bannerImageService.generateMobileImageUrl(anyString())).thenReturn("https://example.com/mobile-image.jpg");
        when(bannerRepository.save(any(Banner.class))).thenAnswer(invocation -> {
            Banner saved = invocation.getArgument(0);
            saved.setTitle("New Title");
            return saved;
        });

        Banner result = bannerService.updateBanner(1L, request, "test-user");

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getUpdatedBy()).isEqualTo("test-user");
        verify(bannerRepository).save(any(Banner.class));
    }

    @Test
    void partialUpdateBanner_shouldUpdateOnlyProvidedFields() {
        Banner existingBanner = Banner.builder()
                .id(1L)
                .title("Old Title")
                .description("Old Description")
                .displayOrder(1)
                .status(BannerStatus.INACTIVE)
                .build();

        BannerPartialUpdateRequest request = BannerPartialUpdateRequest.builder()
                .title("New Title")
                .status(BannerStatus.ACTIVE)
                .altText("New Alt Text")
                .build();

        when(bannerRepository.findById(1L)).thenReturn(Optional.of(existingBanner));
        when(bannerRepository.save(any(Banner.class))).thenReturn(existingBanner);

        Banner result = bannerService.partialUpdateBanner(1L, request, "test-user");

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getDescription()).isEqualTo("Old Description"); // unchanged
        assertThat(result.getStatus()).isEqualTo(BannerStatus.ACTIVE);
        assertThat(result.getAltText()).isEqualTo("New Alt Text");
    }

    @Test
    void updateBannerStatus_shouldUpdateStatus() {
        Banner banner = Banner.builder()
                .id(1L)
                .status(BannerStatus.INACTIVE)
                .build();

        BannerStatusUpdateRequest request = BannerStatusUpdateRequest.builder()
                .status(BannerStatus.ACTIVE)
                .build();

        when(bannerRepository.findById(1L)).thenReturn(Optional.of(banner));
        when(bannerRepository.save(any(Banner.class))).thenReturn(banner);

        Banner result = bannerService.updateBannerStatus(1L, request, "test-user");

        assertThat(result.getStatus()).isEqualTo(BannerStatus.ACTIVE);
        assertThat(result.getUpdatedBy()).isEqualTo("test-user");
    }

    @Test
    void reorderBanners_shouldReorderSuccessfully() {
        Banner banner1 = Banner.builder().id(1L).displayOrder(2).build();
        Banner banner2 = Banner.builder().id(2L).displayOrder(3).build();
        Banner banner3 = Banner.builder().id(3L).displayOrder(1).build();

        BannerReorderRequest request = BannerReorderRequest.builder()
                .banners(Arrays.asList(
                        BannerReorderRequest.BannerOrderItem.builder().id(3L).displayOrder(1).build(),
                        BannerReorderRequest.BannerOrderItem.builder().id(1L).displayOrder(2).build(),
                        BannerReorderRequest.BannerOrderItem.builder().id(2L).displayOrder(3).build()
                ))
                .build();

        when(bannerRepository.findById(3L)).thenReturn(Optional.of(banner3));
        when(bannerRepository.findById(1L)).thenReturn(Optional.of(banner1));
        when(bannerRepository.findById(2L)).thenReturn(Optional.of(banner2));
        when(bannerRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(Arrays.asList(banner3, banner1, banner2));
        when(bannerRepository.save(any(Banner.class))).thenAnswer(invocation -> {
            Banner saved = invocation.getArgument(0);
            return saved;
        });

        List<Banner> result = bannerService.reorderBanners(request, "test-user");

        assertThat(result).hasSize(3);
        verify(bannerRepository, times(3)).save(any(Banner.class));
    }

    @Disabled("Banner feature not fully implemented")
    @Test
    void deleteBanner_shouldSoftDeleteSuccessfully() {
        Banner banner = Banner.builder()
                .id(1L)
                .imageUrl("https://example.com/image.jpg")
                .build();

        when(bannerRepository.findById(1L)).thenReturn(Optional.of(banner));
        when(bannerRepository.save(any(Banner.class))).thenReturn(banner);
        lenient().doNothing().when(bannerImageService).deleteImage(anyString());

        bannerService.deleteBanner(1L, "test-user");

        verify(bannerRepository).save(any(Banner.class));
        verify(bannerImageService).deleteImage("https://example.com/image.jpg");
    }

    @Test
    void restoreBanner_shouldRestoreSuccessfully() {
        Banner deletedBanner = Banner.builder()
                .id(1L)
                .deletedAt(Instant.now())
                .deletedBy("test-user")
                .build();

        when(bannerRepository.findDeletedBannerById(1L)).thenReturn(Optional.of(deletedBanner));
        when(bannerRepository.save(any(Banner.class))).thenReturn(deletedBanner);

        Banner result = bannerService.restoreBanner(1L, "admin-user");

        assertThat(result.getDeletedAt()).isNull();
        assertThat(result.getDeletedBy()).isNull();
        assertThat(result.getUpdatedBy()).isEqualTo("admin-user");
    }

    @Test
    void getDeletedBanners_shouldReturnOnlyDeletedBanners() {
        Banner deletedBanner = Banner.builder()
                .id(1L)
                .deletedAt(Instant.now())
                .build();

        when(bannerRepository.findDeletedBanners())
                .thenReturn(Collections.singletonList(deletedBanner));

        List<Banner> result = bannerService.getDeletedBanners();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeletedAt()).isNotNull();
    }

    @Test
    void getActiveBannersByTargetType_shouldReturnFilteredBanners() {
        Instant now = Instant.now();
        Banner activeBanner = Banner.builder()
                .id(1L)
                .status(BannerStatus.ACTIVE)
                .targetType(BannerTargetType.COURSE)
                .startDate(now.minusSeconds(3600))
                .endDate(now.plusSeconds(3600))
                .displayOrder(1)
                .build();

        when(bannerRepository.findActiveBannersByTargetType(eq(BannerTargetType.COURSE), any(Instant.class)))
                .thenReturn(Collections.singletonList(activeBanner));

        List<PublicBannerResponse> result = bannerService.getActiveBannersByTargetType(BannerTargetType.COURSE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTargetType()).isEqualTo(BannerTargetType.COURSE);
    }

    @Test
    void getActiveBanners_shouldReturnOnlyActiveBanners() {
        Instant now = Instant.now();
        Banner activeBanner = Banner.builder()
                .id(1L)
                .status(BannerStatus.ACTIVE)
                .startDate(now.minusSeconds(3600))
                .endDate(now.plusSeconds(3600))
                .displayOrder(1)
                .build();

        when(bannerRepository.findActiveBanners(any(Instant.class)))
                .thenReturn(Collections.singletonList(activeBanner));

        List<PublicBannerResponse> result = bannerService.getActiveBanners();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void trackImpression_shouldIncrementImpressions() {
        when(bannerRepository.existsById(1L)).thenReturn(true);
        when(bannerRepository.incrementImpressions(eq(1L), any(Instant.class))).thenReturn(1);

        bannerService.trackImpression(1L);

        verify(bannerRepository).incrementImpressions(eq(1L), any(Instant.class));
    }

    @Test
    void trackClick_shouldIncrementClicks() {
        when(bannerRepository.existsById(1L)).thenReturn(true);
        when(bannerRepository.incrementClicks(eq(1L), any(Instant.class))).thenReturn(1);

        bannerService.trackClick(1L);

        verify(bannerRepository).incrementClicks(eq(1L), any(Instant.class));
    }

    @Test
    void trackConversion_shouldIncrementConversions() {
        when(bannerRepository.existsById(1L)).thenReturn(true);
        when(bannerRepository.incrementConversions(1L)).thenReturn(1);

        bannerService.trackConversion(1L);

        verify(bannerRepository).incrementConversions(1L);
    }

    @Test
    void getBannerAnalytics_shouldReturnCorrectCTR() {
        Banner banner = Banner.builder()
                .id(1L)
                .impressions(1000L)
                .clicks(100L)
                .conversions(10L)
                .build();

        when(bannerRepository.findById(1L)).thenReturn(Optional.of(banner));

        BannerAnalyticsResponse result = bannerService.getBannerAnalytics(1L);

        assertThat(result.getBannerId()).isEqualTo(1L);
        assertThat(result.getImpressions()).isEqualTo(1000L);
        assertThat(result.getClicks()).isEqualTo(100L);
        assertThat(result.getConversions()).isEqualTo(10L);
        assertThat(result.getCtr()).isEqualByComparingTo("10.00");
        assertThat(result.getConversionRate()).isEqualByComparingTo("10.00");
    }
}
