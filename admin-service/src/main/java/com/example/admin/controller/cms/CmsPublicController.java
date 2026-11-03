package com.example.admin.controller.cms;

import com.example.admin.client.AdminCourseServiceClient;
import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.cms.HeroDto;
import com.example.admin.service.cms.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Slf4j
public class CmsPublicController {

    private final CmsLandingPageService landingPageService;
    private final CmsHeroService heroService;
    private final CmsNavigationService navigationService;
    private final CmsSectionService sectionService;
    private final CmsAuxiliaryService auxiliaryService;
    private final AdminCourseServiceClient courseServiceClient;

    /**
     * Get published landing page snapshot.
     */
    @GetMapping("/landing-page")
    public ApiResponse<Map<String, Object>> getLandingPage() {
        Map<String, Object> page = landingPageService.getPublishedLandingPage();
        return new ApiResponse<>(true, "Published landing page retrieved successfully", page, LocalDateTime.now().toString());
    }

    /**
     * Get published hero configuration.
     */
    @GetMapping("/landing-page/hero")
    public ApiResponse<HeroDto> getHero() {
        HeroDto hero = heroService.getHero();
        return new ApiResponse<>(true, "Published hero retrieved successfully", hero, LocalDateTime.now().toString());
    }

    /**
     * Get published navigation items.
     */
    @GetMapping("/navigation")
    public ApiResponse<List<?>> getNavigation() {
        List<?> nav = navigationService.getPublicNavigation();
        return new ApiResponse<>(true, "Published navigation items retrieved successfully", nav, LocalDateTime.now().toString());
    }

    /**
     * Get published landing page sections.
     */
    @GetMapping("/landing-page/sections")
    public ApiResponse<List<?>> getSections() {
        List<?> sections = sectionService.getPublicSections();
        return new ApiResponse<>(true, "Published sections retrieved successfully", sections, LocalDateTime.now().toString());
    }

    /**
     * Get published site settings.
     */
    @GetMapping("/site-settings")
    public ApiResponse<Map<String, String>> getSiteSettings() {
        Map<String, String> settings = auxiliaryService.getPublicSiteSettings();
        return new ApiResponse<>(true, "Published site settings retrieved successfully", settings, LocalDateTime.now().toString());
    }

    /**
     * Get published promotional banners (delegates to Course Service).
     */
    @GetMapping("/banners")
    public ApiResponse<List<Map>> getPublicBanners(@RequestParam(value = "position", defaultValue = "home") String position) {
        try {
            List<Map> banners = courseServiceClient.getActiveBannersByTargetType(position.toUpperCase());
            if (banners == null || banners.isEmpty()) {
                banners = courseServiceClient.getAllBanners();
            }
            return new ApiResponse<>(true, "Banners retrieved successfully", banners != null ? banners : List.of(), LocalDateTime.now().toString());
        } catch (Exception e) {
            log.warn("Could not query banners from Course Service: {}", e.getMessage());
            return new ApiResponse<>(true, "Banners fallback", List.of(), LocalDateTime.now().toString());
        }
    }

    /**
     * Get SEO metadata for a page key.
     */
    @GetMapping("/seo/{pageKey}")
    public ApiResponse<?> getSeoMetadata(@PathVariable String pageKey) {
        return new ApiResponse<>(true, "SEO metadata retrieved successfully", auxiliaryService.getPublicSeo(pageKey), LocalDateTime.now().toString());
    }

    /**
     * Get published testimonials.
     */
    @GetMapping("/testimonials")
    public ApiResponse<List<?>> getTestimonials() {
        return new ApiResponse<>(true, "Testimonials retrieved successfully", auxiliaryService.getPublicTestimonials(), LocalDateTime.now().toString());
    }

    /**
     * Get published platform statistics.
     */
    @GetMapping("/website-stats")
    public ApiResponse<List<?>> getWebsiteStats() {
        return new ApiResponse<>(true, "Website statistics retrieved successfully", auxiliaryService.getPublicStatistics(), LocalDateTime.now().toString());
    }

    /**
     * Get published footer configuration.
     */
    @GetMapping("/footer")
    public ApiResponse<Map<String, Object>> getFooter() {
        return new ApiResponse<>(true, "Footer configuration retrieved successfully", auxiliaryService.getPublicFooter(), LocalDateTime.now().toString());
    }

    /**
     * Serve uploaded media files publicly.
     */
    @GetMapping("/media/{fileName}")
    public ResponseEntity<Resource> getMedia(@PathVariable String fileName) {
        try {
            // Prevent path traversal
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return ResponseEntity.badRequest().build();
            }

            Path filePath = Paths.get("uploads/cms").resolve(fileName).normalize();
            File file = filePath.toFile();
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(new FileSystemResource(file));
        } catch (Exception e) {
            log.error("Failed to read media asset {}: {}", fileName, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}

