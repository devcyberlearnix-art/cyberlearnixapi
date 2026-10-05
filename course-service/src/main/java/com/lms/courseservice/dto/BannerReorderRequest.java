package com.lms.courseservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerReorderRequest {

    @NotEmpty(message = "Banner list cannot be empty")
    @Valid
    private List<BannerOrderItem> banners;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BannerOrderItem {

        @NotNull(message = "Banner ID is required")
        private Long id;

        @NotNull(message = "Display order is required")
        private Integer displayOrder;
    }
}
