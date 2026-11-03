package com.example.admin.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CtaDto {
    private String label;
    private String destination;
    @Builder.Default
    private boolean visible = true;
}

