package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String slug;
    private String imageUrl;
    private String thumbnailUrl;
    private String bannerUrl;
    private String heroUrl;
    private String iconUrl;
    private Integer sortOrder;
    private Boolean isActive;
    private Boolean isFeatured;
    private Integer datakartCat;
    private String metaTitle;
    private String metaDescription;
    private Map<String, Object> customAttributes;
    private Integer level;
    private Long parentId;
    private String parentName;
    private String fullPath;
    private Long productCount;
    private List<CategoryResponseDTO> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
