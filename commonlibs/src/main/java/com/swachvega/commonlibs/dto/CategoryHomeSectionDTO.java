package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHomeSectionDTO {

    private CategoryResponseDTO root;
    private List<CategoryResponseDTO> items;
}
