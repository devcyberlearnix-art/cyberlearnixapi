package com.lms.courseservice.dto;

import com.lms.courseservice.entity.Banner.BannerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private BannerStatus status;
}
