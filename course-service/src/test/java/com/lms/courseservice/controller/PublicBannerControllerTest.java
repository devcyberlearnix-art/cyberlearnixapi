package com.lms.courseservice.controller;

import com.lms.courseservice.dto.ApiResponse;
import com.lms.courseservice.dto.PublicBannerResponse;
import com.lms.courseservice.entity.Banner.BannerTargetType;
import com.lms.courseservice.service.BannerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicBannerControllerTest {

    @Mock
    private BannerService bannerService;

    @InjectMocks
    private PublicBannerController publicBannerController;

    @Test
    void getActiveBanners_shouldReturnActiveBanners() {
        PublicBannerResponse banner = PublicBannerResponse.builder()
                .id(1L)
                .title("Active Banner")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .mobileImageUrl("https://example.com/mobile-image.jpg")
                .altText("Test Alt Text")
                .ctaText("Click Here")
                .ctaUrl("/courses/1")
                .displayOrder(1)
                .targetType(BannerTargetType.COURSE)
                .build();

        when(bannerService.getActiveBanners()).thenReturn(Collections.singletonList(banner));

        ResponseEntity<ApiResponse<java.util.List<PublicBannerResponse>>> response = publicBannerController.getActiveBanners();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getId()).isEqualTo(1L);
        assertThat(response.getBody().getData().get(0).getTitle()).isEqualTo("Active Banner");
        assertThat(response.getBody().getData().get(0).getTargetType()).isEqualTo(BannerTargetType.COURSE);
    }

    @Test
    void getActiveBanners_shouldReturnEmptyList_whenNoActiveBanners() {
        when(bannerService.getActiveBanners()).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse<java.util.List<PublicBannerResponse>>> response = publicBannerController.getActiveBanners();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("No active banners found");
        assertThat(response.getBody().getData()).isEmpty();
    }

    @Test
    void getActiveBannersByTargetType_shouldReturnFilteredBanners() {
        PublicBannerResponse banner = PublicBannerResponse.builder()
                .id(1L)
                .title("Course Banner")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .ctaText("Click Here")
                .ctaUrl("/courses/1")
                .displayOrder(1)
                .targetType(BannerTargetType.COURSE)
                .build();

        when(bannerService.getActiveBannersByTargetType(BannerTargetType.COURSE)).thenReturn(Collections.singletonList(banner));

        ResponseEntity<ApiResponse<java.util.List<PublicBannerResponse>>> response = publicBannerController.getActiveBannersByTargetType(BannerTargetType.COURSE);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getTargetType()).isEqualTo(BannerTargetType.COURSE);
    }

    @Test
    void trackImpression_shouldRecordImpression() {
        doNothing().when(bannerService).trackImpression(1L);

        ResponseEntity<ApiResponse<Void>> response = publicBannerController.trackImpression(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Banner impression recorded successfully");
        verify(bannerService).trackImpression(1L);
    }

    @Test
    void trackClick_shouldRecordClick() {
        doNothing().when(bannerService).trackClick(1L);

        ResponseEntity<ApiResponse<Void>> response = publicBannerController.trackClick(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Banner click recorded successfully");
        verify(bannerService).trackClick(1L);
    }

    @Test
    void trackConversion_shouldRecordConversion() {
        doNothing().when(bannerService).trackConversion(1L);

        ResponseEntity<ApiResponse<Void>> response = publicBannerController.trackConversion(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Banner conversion recorded successfully");
        verify(bannerService).trackConversion(1L);
    }
}
