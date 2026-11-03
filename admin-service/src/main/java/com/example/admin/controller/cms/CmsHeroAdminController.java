package com.example.admin.controller.cms;

import com.example.admin.dto.ApiResponse;
import com.example.admin.dto.cms.HeroDto;
import com.example.admin.dto.cms.VisibilityRequest;
import com.example.admin.entity.cms.CmsMediaAsset;
import com.example.admin.service.cms.CmsHeroService;
import com.example.admin.service.cms.CmsMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/landing-page/hero")
@RequiredArgsConstructor
@Slf4j
public class CmsHeroAdminController {

    private final CmsHeroService heroService;
    private final CmsMediaService mediaService;

    /**
     * Get current hero draft configuration.
     */
    @GetMapping
    public ApiResponse<HeroDto> getHero() {
        return new ApiResponse<>(true, "Hero configuration retrieved successfully", heroService.getHero(), LocalDateTime.now().toString());
    }

    /**
     * Update hero configuration.
     */
    @PutMapping
    public ApiResponse<HeroDto> updateHero(@RequestBody HeroDto hero) {
        return new ApiResponse<>(true, "Hero configuration updated successfully", heroService.updateHero(hero), LocalDateTime.now().toString());
    }

    /**
     * Upload an image for the hero section.
     */
    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadHeroImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "imageType", defaultValue = "background") String imageType,
            Authentication authentication) {
        String adminEmail = authentication != null ? authentication.getName() : "ADMIN";
        CmsMediaAsset asset = mediaService.uploadMedia(file, "Hero " + imageType + " image", adminEmail);

        HeroDto hero = heroService.getHero();
        if ("mobile".equalsIgnoreCase(imageType)) {
            hero.setMobileImageUrl(asset.getFileUrl());
        } else {
            hero.setBackgroundImageUrl(asset.getFileUrl());
        }
        heroService.updateHero(hero);

        return new ApiResponse<>(true, "Hero image uploaded and assigned successfully",
                Map.of("imageUrl", asset.getFileUrl(), "imageType", imageType),
                LocalDateTime.now().toString());
    }

    /**
     * Set hero section visibility.
     */
    @PatchMapping("/status")
    public ApiResponse<HeroDto> setHeroVisibility(@RequestBody VisibilityRequest request) {
        boolean visible = request != null && Boolean.TRUE.equals(request.getIsVisible());
        return new ApiResponse<>(true, "Hero visibility updated successfully", heroService.setHeroVisibility(visible), LocalDateTime.now().toString());
    }
}

