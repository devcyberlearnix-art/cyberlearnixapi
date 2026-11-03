package com.example.admin.service.cms;

import com.example.admin.dto.cms.HeroDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CmsHeroService {

    private final CmsLandingPageService landingPageService;
    private final ObjectMapper objectMapper;

    /**
     * Get the current hero configuration from the draft.
     */
    public HeroDto getHero() {
        Map<String, Object> draft = landingPageService.getDraftLandingPage();
        Object heroObj = draft.get("hero");
        if (heroObj != null) {
            try {
                return objectMapper.convertValue(heroObj, HeroDto.class);
            } catch (Exception e) {
                log.warn("Failed to convert hero object: {}", e.getMessage());
            }
        }
        return HeroDto.builder().build();
    }

    /**
     * Update hero configuration in the draft.
     */
    public HeroDto updateHero(HeroDto heroDto) {
        Map<String, Object> draft = landingPageService.getDraftLandingPage();
        draft.put("hero", heroDto);
        landingPageService.updateDraftLandingPage(draft);
        log.info("Hero draft configuration updated successfully");
        return heroDto;
    }

    /**
     * Toggle hero section visibility.
     */
    public HeroDto setHeroVisibility(boolean isVisible) {
        HeroDto hero = getHero();
        hero.setSectionVisible(isVisible);
        return updateHero(hero);
    }
}

