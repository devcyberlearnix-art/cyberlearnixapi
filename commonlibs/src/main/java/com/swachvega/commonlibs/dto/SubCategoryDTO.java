package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryDTO {

    private Long id;

    @NotBlank(message = "SubCategory name is required")
    @Size(max = 100, message = "SubCategory name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Display name is required")
    @Size(max = 150, message = "Display name must not exceed 150 characters")
    private String displayName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Slug must not exceed 100 characters")
    private String slug;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 500, message = "Thumbnail URL must not exceed 500 characters")
    private String thumbnailUrl;

    @Size(max = 500, message = "Card URL must not exceed 500 characters")
    private String cardUrl;

    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    private String bannerUrl;

    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    private Integer sortOrder = 0;

    private Boolean isActive = true;

    private Boolean isFeatured = false;

    private String datakartSubCategoryId;

    private Integer level;

    private Long parentSubCategoryId;

    @Size(max = 200, message = "Meta title must not exceed 200 characters")
    private String metaTitle;

    @Size(max = 500, message = "Meta description must not exceed 500 characters")
    private String metaDescription;

    private Map<String, Object> customAttributes;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    // Read-only fields for response
    private String categoryName;
    private String categoryDisplayName;
    private String fullPath;
    private Long productCount;
    private String createdBy;
    private String updatedBy;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
