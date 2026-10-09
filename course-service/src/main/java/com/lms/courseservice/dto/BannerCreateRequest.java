package com.lms.courseservice.dto;

import com.lms.courseservice.entity.Banner.BannerStatus;
import com.lms.courseservice.entity.Banner.BannerTargetType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotBlank(message = "Image URL is required")
    @Pattern(regexp = "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
             message = "Image URL must be a valid URL")
    private String imageUrl;

    @Pattern(regexp = "^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
             message = "Mobile image URL must be a valid URL")
    private String mobileImageUrl;

    @Size(max = 200, message = "Alt text must not exceed 200 characters")
    private String altText;

    @NotBlank(message = "CTA text is required")
    @Size(max = 100, message = "CTA text must not exceed 100 characters")
    private String ctaText;

    @NotBlank(message = "CTA URL is required")
    @Size(max = 500, message = "CTA URL must not exceed 500 characters")
    private String ctaUrl;

    private Long courseId;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;

    @NotNull(message = "Status is required")
    private BannerStatus status;

    private BannerTargetType targetType;

    private Instant startDate;

    private Instant endDate;
}
