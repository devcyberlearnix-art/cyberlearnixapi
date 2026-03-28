package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHomeSectionsResponseDTO {

    private int page;
    private int size;
    private long totalRoots;
    private int totalPages;
    private List<CategoryHomeSectionDTO> sections;
}
