package com.example.admin.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LandingPageResponseDto {
    private String pageKey;
    private String pageName;
    private String pageStatus;
    private Integer publishedVersion;
    private String publishedAt;
    private HeroDto hero;
    private List<Object> navigation;
    private List<Object> sections;
    private List<Object> testimonials;
    private List<Object> statistics;
    private Map<String, Object> footer;
    private Map<String, Object> siteSettings;
    private Map<String, Object> seo;
}

