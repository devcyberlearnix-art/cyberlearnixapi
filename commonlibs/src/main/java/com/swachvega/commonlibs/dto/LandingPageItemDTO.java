package com.cyberlearnix.commonlibs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LandingPageItemDTO {
    private String id;
    private String image;
    private String title;
    private String cta;
    private String label;
    private String color;
    private String borderColor;
    private String url;
}
