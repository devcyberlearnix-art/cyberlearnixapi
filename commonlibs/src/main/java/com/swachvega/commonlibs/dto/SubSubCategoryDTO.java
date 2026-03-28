package com.cyberlearnix.commonlibs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubSubCategoryDTO {

    private Long id;

    @NotBlank(message = "SubSubCategory name is required")
    @Size(max = 100, message = "SubSubCategory name must not exceed 100 characters")
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

    @Size(max = 500, message = "Icon URL must not exceed 500 characters")
    private String iconUrl;

    private Integer sortOrder = 0;

    private Boolean isActive = true;

    private Boolean isFeatured = false;

    @NotNull(message = "SubCategory ID is required")
    private Long subCategoryId;

    private String subCategoryName;
    private String subCategoryDisplayName;
    private Long productCount;
    private String createdBy;
    private String updatedBy;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
