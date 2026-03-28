package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {

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
    private Long parentId;

    private Map<String, Object> customAttributes;
}
