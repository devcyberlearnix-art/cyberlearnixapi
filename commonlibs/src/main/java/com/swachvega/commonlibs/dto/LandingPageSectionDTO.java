package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandingPageSectionDTO {
    private String id;
    private String type;
    private String title;
    private String action;
    private List<LandingPageItemDTO> data;
}
