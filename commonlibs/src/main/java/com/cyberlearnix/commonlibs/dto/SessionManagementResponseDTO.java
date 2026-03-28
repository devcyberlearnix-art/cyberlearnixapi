package com.cyberlearnix.commonlibs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionManagementResponseDTO {
    private boolean success;
    private String message;
    private List<SessionInfoDTO> sessions;
    private int activeSessionCount;
    private int maxAllowedSessions;
}
