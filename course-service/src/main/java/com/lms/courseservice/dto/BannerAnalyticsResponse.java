package com.lms.courseservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerAnalyticsResponse {

    private Long bannerId;
    private Long impressions;
    private Long clicks;
    private Long conversions;
    private BigDecimal ctr;
    private BigDecimal conversionRate;
    private Instant lastImpressionAt;
    private Instant lastClickAt;
}
