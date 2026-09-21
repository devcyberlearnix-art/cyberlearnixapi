package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerImageUploadRequest {

    private String desktopImageUrl;
    private String mobileImageUrl;
    private Integer imageWidth;
    private Integer imageHeight;
    private String imageFormat;
    private Long imageSize;
}
