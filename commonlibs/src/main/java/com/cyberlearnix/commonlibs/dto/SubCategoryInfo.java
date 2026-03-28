package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryInfo {
    private Long id;
    private String name;
    private String displayName;
    private String slug;
    private String imageUrl;
    private Boolean isActive;
    private Integer sortOrder;

    public SubCategoryInfo(Long id, String name, String displayName) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
    }
}
